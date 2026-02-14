package com.magmaguy.easyminecraftgoals.v1_21_R3.packets;

import com.magmaguy.easyminecraftgoals.internal.FakeItem;
import com.magmaguy.easyminecraftgoals.internal.FakeItemSettings;
import com.magmaguy.easyminecraftgoals.internal.PacketEntityInterface;
import com.magmaguy.easyminecraftgoals.internal.PacketEntityTracker;
import com.magmaguy.easyminecraftgoals.internal.TrackedPacketEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_21_R3.util.CraftChatMessage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FakeItemImpl implements FakeItem, TrackedPacketEntity {

    private final FakeItemSettings settings;
    private Location location;
    private ItemStack currentItemStack;
    private boolean visible = true;
    private final UUID uniqueId = UUID.randomUUID();

    private final Map<UUID, ItemDisplayPacketEntity> playerEntities = new ConcurrentHashMap<>();
    private final List<Runnable> removeCallbacks = new ArrayList<>();
    private int currentVehicleId = -1;
    private Entity vehicleEntity = null;
    private boolean autoTracked = false;
    private boolean valid = true;

    public FakeItemImpl(Location location, FakeItemSettings settings) {
        this.location = location.clone();
        this.settings = new FakeItemSettings(settings);
        this.currentItemStack = settings.getItemStack() != null ? settings.getItemStack().clone() : null;
    }

    @Override
    public void setItemStack(ItemStack itemStack) {
        this.currentItemStack = itemStack != null ? itemStack.clone() : null;
        for (ItemDisplayPacketEntity entity : playerEntities.values()) {
            applyItemStack(entity);
            entity.syncMetadata();
        }
    }

    @Override public ItemStack getItemStack() { return currentItemStack != null ? currentItemStack.clone() : null; }
    @Override public void setVisible(boolean visible) { this.visible = visible; for (ItemDisplayPacketEntity entity : playerEntities.values()) entity.setVisible(visible); }

    @Override
    public void displayTo(Player player) {
        if (player == null) return;
        UUID uuid = player.getUniqueId();
        if (playerEntities.containsKey(uuid)) return;
        ItemDisplayPacketEntity entity = createItemDisplayEntity(location);
        playerEntities.put(uuid, entity);
        entity.displayTo(uuid);
        if (currentVehicleId != -1) entity.mountTo(currentVehicleId);
    }

    @Override public void displayTo(UUID uuid) { Player player = Bukkit.getPlayer(uuid); if (player != null) displayTo(player); }
    @Override public void hideFrom(Player player) { if (player != null) hideFrom(player.getUniqueId()); }
    @Override public void hideFrom(UUID uuid) { ItemDisplayPacketEntity entity = playerEntities.remove(uuid); if (entity != null) entity.remove(); }
    @Override public void teleport(Location location) { this.location = location.clone(); for (ItemDisplayPacketEntity entity : playerEntities.values()) entity.teleport(location); }
    @Override public Location getLocation() { return location.clone(); }
    @Override public void remove() { for (ItemDisplayPacketEntity entity : playerEntities.values()) entity.remove(); playerEntities.clear(); removeCallbacks.forEach(Runnable::run); valid = false; }
    @Override public boolean hasViewers() { return !playerEntities.isEmpty(); }

    @Override
    public void setScale(float scale) {
        for (ItemDisplayPacketEntity entity : playerEntities.values()) {
            ItemDisplay itemDisplay = entity.getBukkitEntity();
            org.bukkit.util.Transformation transform = itemDisplay.getTransformation();
            itemDisplay.setTransformation(new org.bukkit.util.Transformation(transform.getTranslation(), transform.getLeftRotation(), new org.joml.Vector3f(scale, scale, scale), transform.getRightRotation()));
            entity.syncMetadata();
        }
    }

    @Override public void setBillboard(org.bukkit.entity.Display.Billboard billboard) { for (ItemDisplayPacketEntity entity : playerEntities.values()) { entity.<ItemDisplay>getBukkitEntity().setBillboard(billboard); entity.syncMetadata(); } }
    @Override public void setGlowing(boolean glowing) { for (ItemDisplayPacketEntity entity : playerEntities.values()) { entity.getNMSEntity().setGlowingTag(glowing); entity.syncMetadata(); } }
    @Override public void setCustomName(String name) { for (ItemDisplayPacketEntity entity : playerEntities.values()) { entity.getNMSEntity().setCustomName(name != null && !name.isEmpty() ? CraftChatMessage.fromStringOrNull(name) : null); entity.syncMetadata(); } }
    @Override public void setCustomNameVisible(boolean visible) { for (ItemDisplayPacketEntity entity : playerEntities.values()) { entity.getNMSEntity().setCustomNameVisible(visible); entity.syncMetadata(); } }

    @Override
    public void setYawRotation(float yawDegrees) {
        for (ItemDisplayPacketEntity entity : playerEntities.values()) {
            ItemDisplay itemDisplay = entity.getBukkitEntity();
            org.bukkit.util.Transformation transform = itemDisplay.getTransformation();
            float radians = (float) Math.toRadians(yawDegrees);
            org.joml.Quaternionf yawRotation = new org.joml.Quaternionf().rotateY(radians);
            itemDisplay.setTransformation(new org.bukkit.util.Transformation(transform.getTranslation(), transform.getLeftRotation(), transform.getScale(), yawRotation));
            entity.syncMetadata();
        }
    }

    // TrackedPacketEntity implementation
    @Override public Location getTrackingLocation() { return vehicleEntity != null && vehicleEntity.isValid() ? vehicleEntity.getLocation() : location; }
    @Override public World getWorld() { return vehicleEntity != null && vehicleEntity.isValid() ? vehicleEntity.getWorld() : (location != null ? location.getWorld() : null); }
    @Override public void showToPlayer(Player player) { displayTo(player); }
    @Override public void hideFromPlayer(Player player) { hideFrom(player); }
    @Override public boolean isVisibleTo(Player player) { return player != null && playerEntities.containsKey(player.getUniqueId()); }
    @Override public Set<UUID> getCurrentViewers() { return new HashSet<>(playerEntities.keySet()); }
    @Override public boolean isValid() { return valid && (vehicleEntity == null || vehicleEntity.isValid()); }
    @Override public UUID getUniqueId() { return uniqueId; }
    @Override public Entity getVehicle() { return vehicleEntity; }
    @Override public void remount() { if (vehicleEntity != null && vehicleEntity.isValid()) { currentVehicleId = vehicleEntity.getEntityId(); for (ItemDisplayPacketEntity entity : playerEntities.values()) entity.mountTo(currentVehicleId); } }

    public void attachTo(Entity vehicle) {
        if (vehicle == null) return;
        this.vehicleEntity = vehicle;
        this.currentVehicleId = vehicle.getEntityId();
        this.autoTracked = true;
        this.valid = true;
        PacketEntityTracker.getInstance().register(this);
    }

    public void detach() {
        if (autoTracked) {
            PacketEntityTracker.getInstance().unregister(this);
            for (UUID viewerUUID : new HashSet<>(playerEntities.keySet())) { Player player = Bukkit.getPlayer(viewerUUID); if (player != null) hideFrom(player); }
            dismount();
            autoTracked = false;
            valid = false;
        }
    }

    public void mountTo(Entity vehicle) { if (vehicle == null) return; this.currentVehicleId = vehicle.getEntityId(); for (ItemDisplayPacketEntity entity : playerEntities.values()) entity.mountTo(currentVehicleId); }
    public void dismount() { if (currentVehicleId != -1) { for (ItemDisplayPacketEntity entity : playerEntities.values()) entity.dismount(); currentVehicleId = -1; vehicleEntity = null; } }
    public void addRemoveCallback(Runnable callback) { removeCallbacks.add(callback); }

    private ItemDisplayPacketEntity createItemDisplayEntity(Location location) {
        ItemDisplayPacketEntity entity = new ItemDisplayPacketEntity(location);
        ItemDisplay itemDisplay = entity.getBukkitEntity();
        applyItemStack(entity);
        itemDisplay.setBillboard(settings.getBillboard());
        itemDisplay.setViewRange(settings.getViewRange());
        if (settings.getScale() != 1.0f) {
            org.bukkit.util.Transformation transform = itemDisplay.getTransformation();
            float scale = settings.getScale();
            itemDisplay.setTransformation(new org.bukkit.util.Transformation(transform.getTranslation(), transform.getLeftRotation(), new org.joml.Vector3f(scale, scale, scale), transform.getRightRotation()));
        }
        if (settings.isGlowing()) entity.getNMSEntity().setGlowingTag(true);
        if (settings.hasCustomName()) { entity.getNMSEntity().setCustomName(CraftChatMessage.fromStringOrNull(settings.getCustomName())); entity.getNMSEntity().setCustomNameVisible(settings.isCustomNameVisible()); }
        entity.syncMetadata();
        return entity;
    }

    private void applyItemStack(ItemDisplayPacketEntity entity) {
        if (currentItemStack != null) entity.getNMSEntity().setItemStack(CraftItemStack.asNMSCopy(currentItemStack));
    }

    private static class ItemDisplayPacketEntity extends AbstractPacketEntity<Display.ItemDisplay> {
        public ItemDisplayPacketEntity(Location location) { super(location); }
        @Override protected Display.ItemDisplay createEntity(Location location) { return new Display.ItemDisplay(EntityType.ITEM_DISPLAY, getNMSLevel(location)); }
    }
}
