package com.magmaguy.easyminecraftgoals.v1_21_R7_common.packets;

import com.magmaguy.easyminecraftgoals.internal.FakeText;
import com.magmaguy.easyminecraftgoals.internal.FakeTextSettings;
import com.magmaguy.easyminecraftgoals.internal.PacketEntityInterface;
import com.magmaguy.easyminecraftgoals.thirdparty.BedrockChecker;
import com.magmaguy.easyminecraftgoals.v1_21_R7_common.CraftBukkitBridge;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FakeText implementation that uses TextDisplay for Java Edition players
 * and ArmorStand for Bedrock Edition players.
 */
public class FakeTextImpl implements FakeText {

    private final FakeTextSettings settings;
    private Location location;
    private String currentText;
    private boolean visible = true;

    // Track per-player entities - UUID -> packet entity
    private final Map<UUID, PacketEntityInterface> playerEntities = new ConcurrentHashMap<>();
    // Track which players are using ArmorStand (Bedrock)
    private final Set<UUID> bedrockPlayers = ConcurrentHashMap.newKeySet();

    private final List<Runnable> removeCallbacks = new ArrayList<>();

    public FakeTextImpl(Location location, FakeTextSettings settings) {
        this.location = location.clone();
        this.settings = new FakeTextSettings(settings);
        this.currentText = settings.getText();
    }

    @Override
    public void setText(String text) {
        this.currentText = text;
        // Update all existing entities
        for (Map.Entry<UUID, PacketEntityInterface> entry : playerEntities.entrySet()) {
            UUID uuid = entry.getKey();
            PacketEntityInterface entity = entry.getValue();

            if (bedrockPlayers.contains(uuid)) {
                // ArmorStand - set custom name
                ArmorStand armorStand = entity.getBukkitEntity();
                armorStand.setCustomNameVisible(true);
                armorStand.setCustomName(Component.literal(text));
            } else {
                // TextDisplay - set text directly
                TextDisplay textDisplay = entity.getBukkitEntity();
                textDisplay.setText(text);
            }
            entity.syncMetadata();
        }
    }

    @Override
    public String getText() {
        return currentText;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
        for (PacketEntityInterface entity : playerEntities.values()) {
            entity.setVisible(visible);
        }
    }

    @Override
    public void displayTo(Player player) {
        if (player == null) return;

        UUID uuid = player.getUniqueId();

        // Already displayed to this player
        if (playerEntities.containsKey(uuid)) return;

        boolean isBedrock = BedrockChecker.isBedrock(player);
        PacketEntityInterface entity;

        if (isBedrock) {
            // Create ArmorStand for Bedrock players
            entity = createArmorStandEntity(location);
            bedrockPlayers.add(uuid);
        } else {
            // Create TextDisplay for Java players
            entity = createTextDisplayEntity(location);
        }

        playerEntities.put(uuid, entity);
        entity.displayTo(uuid);
    }

    @Override
    public void displayTo(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            displayTo(player);
        }
    }

    @Override
    public void hideFrom(Player player) {
        if (player == null) return;
        hideFrom(player.getUniqueId());
    }

    @Override
    public void hideFrom(UUID uuid) {
        PacketEntityInterface entity = playerEntities.remove(uuid);
        if (entity != null) {
            entity.remove();
        }
        bedrockPlayers.remove(uuid);
    }

    @Override
    public void teleport(Location location) {
        this.location = location.clone();
        for (PacketEntityInterface entity : playerEntities.values()) {
            entity.teleport(location);
        }
    }

    @Override
    public Location getLocation() {
        return location.clone();
    }

    @Override
    public void remove() {
        for (PacketEntityInterface entity : playerEntities.values()) {
            entity.remove();
        }
        playerEntities.clear();
        bedrockPlayers.clear();
        removeCallbacks.forEach(Runnable::run);
    }

    @Override
    public boolean hasViewers() {
        return !playerEntities.isEmpty();
    }

    /**
     * Adds a callback to be run when this FakeText is removed.
     */
    public void addRemoveCallback(Runnable callback) {
        removeCallbacks.add(callback);
    }

    /**
     * Creates a TextDisplay packet entity with all the styling applied.
     */
    private PacketEntityInterface createTextDisplayEntity(Location location) {
        TextDisplayPacketEntity entity = new TextDisplayPacketEntity(location);

        // Apply settings to the TextDisplay
        TextDisplay textDisplay = entity.getBukkitEntity();
        textDisplay.setText(currentText);

        // Billboard mode
        textDisplay.setBillboard(settings.getBillboard());

        // Background color
        if (settings.hasBackgroundColor()) {
            if (settings.getBackgroundColor() != null) {
                textDisplay.setBackgroundColor(settings.getBackgroundColor());
            } else {
                textDisplay.setBackgroundColor(Color.fromARGB(settings.getBackgroundArgb()));
            }
        }

        // Text opacity
        if (settings.getTextOpacity() != (byte) -1) {
            textDisplay.setTextOpacity(settings.getTextOpacity());
        }

        // Shadow
        textDisplay.setShadowed(settings.hasShadow());

        // See through
        textDisplay.setSeeThrough(settings.isSeeThrough());

        // Line width
        textDisplay.setLineWidth(settings.getLineWidth());

        // View range
        textDisplay.setViewRange(settings.getViewRange());

        // Alignment
        switch (settings.getAlignment()) {
            case LEFT -> textDisplay.setAlignment(TextDisplay.TextAlignment.LEFT);
            case RIGHT -> textDisplay.setAlignment(TextDisplay.TextAlignment.RIGHT);
            default -> textDisplay.setAlignment(TextDisplay.TextAlignment.CENTER);
        }

        // Scale - applied via transformation
        if (settings.getScale() != 1.0f) {
            org.bukkit.util.Transformation transform = textDisplay.getTransformation();
            textDisplay.setTransformation(new org.bukkit.util.Transformation(
                    transform.getTranslation(),
                    transform.getLeftRotation(),
                    new org.joml.Vector3f(settings.getScale(), settings.getScale(), settings.getScale()),
                    transform.getRightRotation()
            ));
        }

        entity.syncMetadata();
        return entity;
    }

    /**
     * Creates an ArmorStand packet entity for Bedrock players.
     * Most TextDisplay styling is ignored - only text and visibility matter.
     */
    private PacketEntityInterface createArmorStandEntity(Location location) {
        ArmorStandTextEntity entity = new ArmorStandTextEntity(location);

        // Set the text as custom name
        ArmorStand armorStand = entity.getNMSEntity();
        armorStand.setInvisible(true);
        armorStand.setMarker(true);
        armorStand.setCustomNameVisible(true);
        armorStand.setCustomName(Component.literal(currentText));

        entity.syncMetadata();
        return entity;
    }

    /**
     * Internal TextDisplay packet entity class.
     */
    private static class TextDisplayPacketEntity extends AbstractPacketEntity<Display.TextDisplay> {
        public TextDisplayPacketEntity(Location location) {
            super(location);
        }

        @Override
        protected Display.TextDisplay createEntity(Location location) {
            return new Display.TextDisplay(EntityType.TEXT_DISPLAY, getNMSLevel(location));
        }
    }

    /**
     * Internal ArmorStand packet entity class for text.
     */
    private static class ArmorStandTextEntity extends AbstractPacketEntity<ArmorStand> {
        public ArmorStandTextEntity(Location location) {
            super(location);
        }

        @Override
        protected ArmorStand createEntity(Location location) {
            return new ArmorStand(EntityType.ARMOR_STAND, getNMSLevel(location));
        }
    }
}
