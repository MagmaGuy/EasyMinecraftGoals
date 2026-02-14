package com.magmaguy.easyminecraftgoals.v1_21_R4.packets;

import com.magmaguy.easyminecraftgoals.internal.FakeText;
import com.magmaguy.easyminecraftgoals.internal.FakeTextSettings;
import com.magmaguy.easyminecraftgoals.internal.PacketEntityInterface;
import com.magmaguy.easyminecraftgoals.internal.PacketEntityTracker;
import com.magmaguy.easyminecraftgoals.internal.TrackedPacketEntity;
import com.magmaguy.easyminecraftgoals.thirdparty.BedrockChecker;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R4.util.CraftChatMessage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FakeTextImpl implements FakeText, TrackedPacketEntity {

    private final FakeTextSettings settings;
    private Location location;
    private String currentText;
    private boolean visible = true;
    private final UUID uniqueId = UUID.randomUUID();

    private final Map<UUID, PacketEntityInterface> playerEntities = new ConcurrentHashMap<>();
    private final Set<UUID> bedrockPlayers = ConcurrentHashMap.newKeySet();
    private final List<Runnable> removeCallbacks = new ArrayList<>();
    private int currentVehicleId = -1;
    private Entity vehicleEntity = null;
    private boolean autoTracked = false;
    private boolean valid = true;

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
                @SuppressWarnings("unchecked")
                ArmorStand as = ((AbstractPacketEntity<ArmorStand>) entity).getNMSEntity();
                as.setCustomNameVisible(true);
                as.setCustomName(CraftChatMessage.fromStringOrNull(text));
            } else {
                TextDisplay td = entity.getBukkitEntity();
                td.setText(text);
            }
            entity.syncMetadata();
        }
    }

    @Override public String getText() { return currentText; }
    @Override public void setVisible(boolean visible) { this.visible = visible; for (PacketEntityInterface e : playerEntities.values()) e.setVisible(visible); }

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
        if (currentVehicleId != -1) entity.mountTo(currentVehicleId);
    }

    @Override public void displayTo(UUID uuid) { Player p = Bukkit.getPlayer(uuid); if (p != null) displayTo(p); }
    @Override public void hideFrom(Player player) { if (player != null) hideFrom(player.getUniqueId()); }
    @Override public void hideFrom(UUID uuid) { PacketEntityInterface e = playerEntities.remove(uuid); if (e != null) e.remove(); bedrockPlayers.remove(uuid); }
    @Override public void teleport(Location loc) { this.location = loc.clone(); for (PacketEntityInterface e : playerEntities.values()) e.teleport(loc); }
    @Override public Location getLocation() { return location.clone(); }
    @Override public void remove() { for (PacketEntityInterface e : playerEntities.values()) e.remove(); playerEntities.clear(); bedrockPlayers.clear(); removeCallbacks.forEach(Runnable::run); }
    @Override public boolean hasViewers() { return !playerEntities.isEmpty(); }

    @Override public void setTextOpacity(byte opacity) { for (Map.Entry<UUID, PacketEntityInterface> entry : playerEntities.entrySet()) { if (bedrockPlayers.contains(entry.getKey())) continue; TextDisplay td = entry.getValue().getBukkitEntity(); td.setTextOpacity(opacity); entry.getValue().syncMetadata(); } }
    @Override public void setScale(float scale) { for (Map.Entry<UUID, PacketEntityInterface> entry : playerEntities.entrySet()) { if (bedrockPlayers.contains(entry.getKey())) continue; TextDisplay td = entry.getValue().getBukkitEntity(); var t = td.getTransformation(); td.setTransformation(new org.bukkit.util.Transformation(t.getTranslation(), t.getLeftRotation(), new org.joml.Vector3f(scale, scale, scale), t.getRightRotation())); entry.getValue().syncMetadata(); } }
    @Override public void setBackgroundColor(Color color) { for (Map.Entry<UUID, PacketEntityInterface> entry : playerEntities.entrySet()) { if (bedrockPlayers.contains(entry.getKey())) continue; TextDisplay td = entry.getValue().getBukkitEntity(); td.setBackgroundColor(color); entry.getValue().syncMetadata(); } }
    @Override public void setBackgroundColor(int argb) { setBackgroundColor(Color.fromARGB(argb)); }
    @Override public void setShadowed(boolean shadow) { for (Map.Entry<UUID, PacketEntityInterface> entry : playerEntities.entrySet()) { if (bedrockPlayers.contains(entry.getKey())) continue; TextDisplay td = entry.getValue().getBukkitEntity(); td.setShadowed(shadow); entry.getValue().syncMetadata(); } }
    @Override public void setSeeThrough(boolean seeThrough) { for (Map.Entry<UUID, PacketEntityInterface> entry : playerEntities.entrySet()) { if (bedrockPlayers.contains(entry.getKey())) continue; TextDisplay td = entry.getValue().getBukkitEntity(); td.setSeeThrough(seeThrough); entry.getValue().syncMetadata(); } }
    @Override public void setBillboard(org.bukkit.entity.Display.Billboard billboard) { for (Map.Entry<UUID, PacketEntityInterface> entry : playerEntities.entrySet()) { if (bedrockPlayers.contains(entry.getKey())) continue; TextDisplay td = entry.getValue().getBukkitEntity(); td.setBillboard(billboard); entry.getValue().syncMetadata(); } }

    @Override public void mountTo(Entity vehicle) { if (vehicle == null) return; this.currentVehicleId = vehicle.getEntityId(); for (PacketEntityInterface entity : playerEntities.values()) entity.mountTo(currentVehicleId); }
    @Override public void dismount() { if (currentVehicleId != -1) { for (PacketEntityInterface entity : playerEntities.values()) entity.dismount(); currentVehicleId = -1; vehicleEntity = null; } }

    @Override
    public void attachTo(Entity vehicle) {
        if (vehicle == null) return;
        this.vehicleEntity = vehicle;
        this.currentVehicleId = vehicle.getEntityId();
        this.autoTracked = true;
        this.valid = true;
        PacketEntityTracker.getInstance().register(this);
    }

    @Override
    public void detach() {
        if (autoTracked) {
            PacketEntityTracker.getInstance().unregister(this);
            for (UUID viewerUUID : new HashSet<>(playerEntities.keySet())) { Player player = Bukkit.getPlayer(viewerUUID); if (player != null) hideFrom(player); }
            dismount();
            autoTracked = false;
            valid = false;
        }
    }

    @Override public Entity getVehicle() { return vehicleEntity; }
    @Override public boolean isAutoTracked() { return autoTracked; }

    // TrackedPacketEntity implementation
    @Override public Location getTrackingLocation() { return vehicleEntity != null && vehicleEntity.isValid() ? vehicleEntity.getLocation() : location; }
    @Override public World getWorld() { return vehicleEntity != null && vehicleEntity.isValid() ? vehicleEntity.getWorld() : (location != null ? location.getWorld() : null); }
    @Override public void showToPlayer(Player player) { displayTo(player); }
    @Override public void hideFromPlayer(Player player) { hideFrom(player); }
    @Override public boolean isVisibleTo(Player player) { return player != null && playerEntities.containsKey(player.getUniqueId()); }
    @Override public Set<UUID> getCurrentViewers() { return new HashSet<>(playerEntities.keySet()); }
    @Override public boolean isValid() { return valid && (vehicleEntity == null || vehicleEntity.isValid()); }
    @Override public UUID getUniqueId() { return uniqueId; }
    @Override public void remount() { if (vehicleEntity != null && vehicleEntity.isValid()) { currentVehicleId = vehicleEntity.getEntityId(); for (PacketEntityInterface entity : playerEntities.values()) entity.mountTo(currentVehicleId); } }

    public void addRemoveCallback(Runnable callback) { removeCallbacks.add(callback); }

    private PacketEntityInterface createTextDisplayEntity(Location location) {
        TextDisplayPacketEntity entity = new TextDisplayPacketEntity(location);
        TextDisplay td = entity.getBukkitEntity();
        td.setText(currentText);
        td.setBillboard(settings.getBillboard());
        if (settings.hasBackgroundColor()) td.setBackgroundColor(settings.getBackgroundColor() != null ? settings.getBackgroundColor() : Color.fromARGB(settings.getBackgroundArgb()));
        if (settings.getTextOpacity() != (byte) -1) td.setTextOpacity(settings.getTextOpacity());
        td.setShadowed(settings.hasShadow()); td.setSeeThrough(settings.isSeeThrough()); td.setLineWidth(settings.getLineWidth()); td.setViewRange(settings.getViewRange());
        switch (settings.getAlignment()) { case LEFT -> td.setAlignment(TextDisplay.TextAlignment.LEFT); case RIGHT -> td.setAlignment(TextDisplay.TextAlignment.RIGHT); default -> td.setAlignment(TextDisplay.TextAlignment.CENTER); }
        if (settings.getScale() != 1.0f || settings.hasTranslation()) {
            var t = td.getTransformation();
            org.joml.Vector3f translation = settings.hasTranslation() ? new org.joml.Vector3f(settings.getTranslationX(), settings.getTranslationY(), settings.getTranslationZ()) : t.getTranslation();
            org.joml.Vector3f scale = settings.getScale() != 1.0f ? new org.joml.Vector3f(settings.getScale(), settings.getScale(), settings.getScale()) : t.getScale();
            td.setTransformation(new org.bukkit.util.Transformation(translation, t.getLeftRotation(), scale, t.getRightRotation()));
        }
        entity.syncMetadata();
        return entity;
    }

    private PacketEntityInterface createArmorStandEntity(Location location) {
        ArmorStandTextEntity entity = new ArmorStandTextEntity(location);
        ArmorStand as = entity.getNMSEntity();
        as.setInvisible(true); as.setMarker(true); as.setCustomNameVisible(true); as.setCustomName(CraftChatMessage.fromStringOrNull(currentText));
        entity.syncMetadata();
        return entity;
    }

    private static class TextDisplayPacketEntity extends AbstractPacketEntity<Display.TextDisplay> {
        public TextDisplayPacketEntity(Location location) { super(location); }
        @Override protected Display.TextDisplay createEntity(Location location) { return new Display.TextDisplay(EntityType.TEXT_DISPLAY, ((CraftWorld) location.getWorld()).getHandle()); }
    }

    private static class ArmorStandTextEntity extends AbstractPacketEntity<ArmorStand> {
        public ArmorStandTextEntity(Location location) { super(location); }
        @Override protected ArmorStand createEntity(Location location) { return new ArmorStand(EntityType.ARMOR_STAND, ((CraftWorld) location.getWorld()).getHandle()); }
    }
}
