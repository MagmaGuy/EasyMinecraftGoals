package com.magmaguy.easyminecraftgoals.v1_21_R7_paper.packets;

import com.google.common.collect.Sets;
import com.magmaguy.easyminecraftgoals.internal.PacketEntityInterface;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class AbstractPacketEntity<T extends Entity> implements PacketEntityInterface {
    protected final T entity;
    private final Set<UUID> viewers = Sets.newConcurrentHashSet();
    private final List<Runnable> removeCallbacks = new LinkedList<>();
    protected boolean visible = true;
    private final int EntityID;

    protected AbstractPacketEntity(Location location) {
        this.entity = createEntity(location);
        EntityID = entity.getId();
    }

    public List<Player> getViewersAsPlayers() {
        List<Player> players = new ArrayList<>();
        for (UUID viewer : viewers) {
            Player player = Bukkit.getPlayer(viewer);
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }

    @Override
    public boolean hasViewers() {
        return !viewers.isEmpty();
    }

    protected abstract T createEntity(Location location);

    @Override
    public void addViewer(UUID player) {
        viewers.add(player);
    }

    @Override
    public void removeViewer(UUID player) {
        viewers.remove(player);
    }

    public void addRemoveCallback(Runnable callback) {
        removeCallbacks.add(callback);
    }

    protected Level getNMSLevel(Location location) {
        return ((CraftWorld) location.getWorld()).getHandle();
    }

    public T getNMSEntity() {
        return entity;
    }

    public <B extends org.bukkit.entity.Entity> B getBukkitEntity() {
        return (B) entity.getBukkitEntity();
    }

    public Location getLocation() {
        return new Location(entity.level().getWorld(), entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
    }

    public UUID getUniqueId() {
        return entity.getUUID();
    }

    //Entity creation
    protected Packet<?> createEntityDataPacket() {
        List<SynchedEntityData.DataValue<?>> dataValues = entity.getEntityData().getNonDefaultValues();

        if (dataValues == null) {
            return null;
        }

        return new ClientboundSetEntityDataPacket(EntityID, dataValues);
    }

    public Packet<?> generateRemovePacket() {
        return new ClientboundRemoveEntitiesPacket(EntityID);
    }

    //Entity destruction
    public void remove() {
        // Broadcast remove to all viewers
        sendPacketToAll(generateRemovePacket());
        removeCallbacks.forEach(Runnable::run);
    }

    //Visibility - Global visibility changes affect all viewers
    public Packet<?> generateSetVisiblePacket(boolean visible) {
        this.visible = visible;

        if (visible) {
            return createEntityDataPacket();
        } else {
            return new ClientboundRemoveEntitiesPacket(EntityID);
        }
    }

    public void setVisible(boolean visible) {
        // This is a global visibility change - send to all
        sendPacketToAll(generateSetVisiblePacket(visible));
    }

    // Hide from specific player - FIXED
    public void hideFrom(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            // Player is offline, just remove from viewers
            removeViewer(uuid);
            return;
        }

        // Send remove packet ONLY to this specific player
        sendPacketToPlayer(player, new ClientboundRemoveEntitiesPacket(EntityID));

        // THEN remove from viewers list
        removeViewer(uuid);
    }

    // Display to specific player - Already correct, but cleaned up
    public void displayTo(Player player) {
        if (viewers.contains(player.getUniqueId())) {
            return;
        }

        // Add to viewers first
        addViewer(player.getUniqueId());

        // Send packets ONLY to this specific player
        sendPacketToPlayer(player,
                new ClientboundAddEntityPacket(
                        EntityID,
                        entity.getUUID(),
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        entity.getXRot(),
                        entity.getYRot(),
                        entity.getType(),
                        0,
                        new Vec3(0, 0, 0), 0),
                createEntityDataPacket()
        );
    }

    //Teleports - affects all viewers
    public void teleport(Location location) {
        entity.teleportTo(location.getX(), location.getY(), location.getZ());
        sendTeleportPacket();
    }

    protected void sendTeleportPacket() {
        sendPacketToAll(generateTeleportPacket());
    }

    protected Packet<?> generateTeleportPacket() {
        // Need to include the actual yaw and pitch!
        return new ClientboundTeleportEntityPacket(
                EntityID,
                new PositionMoveRotation(
                        entity.position(),
                        new Vec3(0, 0, 0),
                        entity.getYRot(),  // Use actual yaw
                        entity.getXRot()   // Use actual pitch
                ),
                new HashSet<>(),
                true
        );
    }

    // Also update generateMovePacket to ensure yaw is handled correctly
    public Packet<?> generateMovePacket(Location location) {
        // Check if we have viewers first
        if (viewers.isEmpty()) {
            return null;
        }

        // Update entity position AND rotation
        entity.setPos(location.getX(), location.getY(), location.getZ());
        entity.setYRot(location.getYaw());  // Keep the yaw from location
        entity.setXRot(location.getPitch()); // Keep the pitch from location

        // Use teleport packet for absolute positioning
        return generateTeleportPacket();
    }

    public void move(Location location) {
        sendPacketToAll(generateMovePacket(location));
    }

    // ==== Packet sending methods - RENAMED AND CLARIFIED ====

    /**
     * Send packets to a specific player only
     */
    protected void sendPacketToPlayer(Player player, Packet<?>... nmsPackets) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();

        for (Packet<?> nmsPacket : nmsPackets) {
            if (nmsPacket == null) {
                continue;
            }
            nmsPlayer.connection.send(nmsPacket);
        }
    }

    /**
     * Broadcast packets to ALL current viewers
     * Use this for movement, teleports, and global state changes
     */
    protected void sendPacketToAll(Packet<?> nmsPacket) {
        if (nmsPacket == null) return;

        for (UUID viewer : viewers) {
            Player player = Bukkit.getPlayer(viewer);
            if (player == null) {
                viewers.remove(viewer);
                continue;
            }
            sendPacketToPlayer(player, nmsPacket);
        }
    }

    /**
     * @deprecated Use sendPacketToAll or sendPacketToPlayer for clarity
     */
    @Deprecated
    protected void sendPacket(Player player, Packet<?>... nmsPackets) {
        sendPacketToPlayer(player, nmsPackets);
    }

    /**
     * @deprecated Use sendPacketToAll for clarity
     */
    @Deprecated
    protected void sendPacket(Packet<?> nmsPacket) {
        sendPacketToAll(nmsPacket);
    }

    //Create packet bundle
    @Override
    public PacketBundle createPacketBundle() {
        return new PacketBundle();
    }

    protected ServerPlayer getNMSPlayer(Player bukkitPlayer) {
        return ((CraftPlayer) bukkitPlayer).getHandle();
    }
}
