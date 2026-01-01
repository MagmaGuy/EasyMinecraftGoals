package com.magmaguy.easyminecraftgoals.v1_21_R5.packets;

import com.magmaguy.easyminecraftgoals.internal.FakeText;
import com.magmaguy.easyminecraftgoals.internal.FakeTextSettings;
import com.magmaguy.easyminecraftgoals.internal.PacketEntityInterface;
import com.magmaguy.easyminecraftgoals.thirdparty.BedrockChecker;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R5.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FakeTextImpl implements FakeText {

    private final FakeTextSettings settings;
    private Location location;
    private String currentText;
    private boolean visible = true;

    private final Map<UUID, PacketEntityInterface> playerEntities = new ConcurrentHashMap<>();
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
        for (Map.Entry<UUID, PacketEntityInterface> entry : playerEntities.entrySet()) {
            UUID uuid = entry.getKey();
            PacketEntityInterface entity = entry.getValue();

            if (bedrockPlayers.contains(uuid)) {
                ArmorStand armorStand = entity.getBukkitEntity();
                armorStand.setCustomNameVisible(true);
                armorStand.setCustomName(Component.literal(text));
            } else {
                TextDisplay textDisplay = entity.getBukkitEntity();
                textDisplay.setText(text);
            }
            entity.syncMetadata();
        }
    }

    @Override
    public String getText() { return currentText; }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
        for (PacketEntityInterface entity : playerEntities.values()) entity.setVisible(visible);
    }

    @Override
    public void displayTo(Player player) {
        if (player == null) return;
        UUID uuid = player.getUniqueId();
        if (playerEntities.containsKey(uuid)) return;

        boolean isBedrock = BedrockChecker.isBedrock(player);
        PacketEntityInterface entity = isBedrock ? createArmorStandEntity(location) : createTextDisplayEntity(location);
        if (isBedrock) bedrockPlayers.add(uuid);

        playerEntities.put(uuid, entity);
        entity.displayTo(uuid);
    }

    @Override
    public void displayTo(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) displayTo(player);
    }

    @Override
    public void hideFrom(Player player) { if (player != null) hideFrom(player.getUniqueId()); }

    @Override
    public void hideFrom(UUID uuid) {
        PacketEntityInterface entity = playerEntities.remove(uuid);
        if (entity != null) entity.remove();
        bedrockPlayers.remove(uuid);
    }

    @Override
    public void teleport(Location location) {
        this.location = location.clone();
        for (PacketEntityInterface entity : playerEntities.values()) entity.teleport(location);
    }

    @Override
    public Location getLocation() { return location.clone(); }

    @Override
    public void remove() {
        for (PacketEntityInterface entity : playerEntities.values()) entity.remove();
        playerEntities.clear();
        bedrockPlayers.clear();
        removeCallbacks.forEach(Runnable::run);
    }

    @Override
    public boolean hasViewers() { return !playerEntities.isEmpty(); }

    private PacketEntityInterface createTextDisplayEntity(Location location) {
        TextDisplayPacketEntity entity = new TextDisplayPacketEntity(location);
        TextDisplay td = entity.getBukkitEntity();
        td.setText(currentText);
        td.setBillboard(settings.getBillboard());
        if (settings.hasBackgroundColor()) {
            td.setBackgroundColor(settings.getBackgroundColor() != null ? settings.getBackgroundColor() : Color.fromARGB(settings.getBackgroundArgb()));
        }
        if (settings.getTextOpacity() != (byte) -1) td.setTextOpacity(settings.getTextOpacity());
        td.setShadowed(settings.hasShadow());
        td.setSeeThrough(settings.isSeeThrough());
        td.setLineWidth(settings.getLineWidth());
        td.setViewRange(settings.getViewRange());
        switch (settings.getAlignment()) {
            case LEFT -> td.setAlignment(TextDisplay.TextAlignment.LEFT);
            case RIGHT -> td.setAlignment(TextDisplay.TextAlignment.RIGHT);
            default -> td.setAlignment(TextDisplay.TextAlignment.CENTER);
        }
        if (settings.getScale() != 1.0f) {
            org.bukkit.util.Transformation t = td.getTransformation();
            td.setTransformation(new org.bukkit.util.Transformation(t.getTranslation(), t.getLeftRotation(),
                    new org.joml.Vector3f(settings.getScale(), settings.getScale(), settings.getScale()), t.getRightRotation()));
        }
        entity.syncMetadata();
        return entity;
    }

    private PacketEntityInterface createArmorStandEntity(Location location) {
        ArmorStandTextEntity entity = new ArmorStandTextEntity(location);
        ArmorStand as = entity.getNMSEntity();
        as.setInvisible(true);
        as.setMarker(true);
        as.setCustomNameVisible(true);
        as.setCustomName(Component.literal(currentText));
        entity.syncMetadata();
        return entity;
    }

    private static class TextDisplayPacketEntity extends AbstractPacketEntity<Display.TextDisplay> {
        public TextDisplayPacketEntity(Location location) { super(location); }
        @Override
        protected Display.TextDisplay createEntity(Location location) {
            return new Display.TextDisplay(EntityType.TEXT_DISPLAY, ((CraftWorld) location.getWorld()).getHandle());
        }
    }

    private static class ArmorStandTextEntity extends AbstractPacketEntity<ArmorStand> {
        public ArmorStandTextEntity(Location location) { super(location); }
        @Override
        protected ArmorStand createEntity(Location location) {
            return new ArmorStand(EntityType.ARMOR_STAND, ((CraftWorld) location.getWorld()).getHandle());
        }
    }
}
