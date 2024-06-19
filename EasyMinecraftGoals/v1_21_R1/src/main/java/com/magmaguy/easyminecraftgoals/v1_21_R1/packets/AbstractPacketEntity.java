package com.magmaguy.easyminecraftgoals.v1_21_R1.packets;

import com.google.common.collect.Sets;
import com.magmaguy.easyminecraftgoals.internal.PacketEntityInterface;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractPacketEntity<T extends Entity> implements PacketEntityInterface {
    protected final T entity;
    private final Set<UUID> viewers = Sets.newConcurrentHashSet();
    private final List<Runnable> removeCallbacks = new LinkedList<>();
    protected boolean visible = true;

    protected AbstractPacketEntity(Location location) {
        this.entity = createEntity(location);
        this.teleport(location);
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

    public void displayTo(Player player) {
        if (viewers.contains(player.getUniqueId())) {
            return;
        }

        viewers.add(player.getUniqueId());

        sendPacket(player, new ClientboundAddEntityPacket(
                entity.getId(),
                entity.getUUID(),
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                0,
                0,
                entity.getType(),
                0,
                new Vec3(0,0,0), 0));
        sendPacket(player, createEntityDataPacket());
        addViewer(player.getUniqueId());
    }

    public void hideFrom(UUID uuid) {
        if (!viewers.contains(uuid)) {
            return;
        }

        viewers.remove(uuid);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) sendPacket(player, new ClientboundRemoveEntitiesPacket(entity.getId()));
        removeViewer(uuid);
    }

    public void remove() {
        sendPacket(new ClientboundRemoveEntitiesPacket(entity.getId()));
        removeCallbacks.forEach(Runnable::run);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;

        if (visible) {
            sendPacket(createEntityDataPacket());
        } else {
            sendPacket(new ClientboundRemoveEntitiesPacket(entity.getId()));
        }
    }

    protected Packet<?> createEntityDataPacket() {
        List<SynchedEntityData.DataValue<?>> dataValues = entity.getEntityData().getNonDefaultValues();

        if (dataValues == null) {
            return null;
        }

        return new ClientboundSetEntityDataPacket(entity.getId(), dataValues);
    }

    private void updatePosition(Location oldPos) {
        if (viewers.isEmpty()) {
            return;
        }

        Location newPos = getLocation();

        if (oldPos.getWorld() != newPos.getWorld()) {
            sendTeleportPacket();
            return;
        }

        double deltaX = newPos.getX() - oldPos.getX();
        double deltaY = newPos.getY() - oldPos.getY();
        double deltaZ = newPos.getZ() - oldPos.getZ();

        if (deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > 16 * 16) {
            sendTeleportPacket();
            return;
        }

        sendPacket(new ClientboundMoveEntityPacket.PosRot(entity.getId(),
                (short) (deltaX * 4096),
                (short) (deltaY * 4096),
                (short) (deltaZ * 4096),
                (byte) (newPos.getYaw() * 256 / 360),
                (byte) (newPos.getPitch() * 256 / 360),
                true)
        );
    }

    protected void sendTeleportPacket() {
        sendPacket(new ClientboundTeleportEntityPacket(entity));
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

    public void teleport(Location location) {
        Location oldPos = getLocation();
        entity.moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        updatePosition(oldPos);
    }


    protected void sendPacket(Player player, Packet<?>... nmsPackets) {
        ServerPlayer nmsPlayer = getNMSPlayer(player);

        for (Packet<?> nmsPacket : nmsPackets) {
            if (nmsPacket == null) {
                continue;
            }

            nmsPlayer.connection.send(nmsPacket);
        }
    }

    protected void sendPacket(Packet<?> nmsPacket) {
        for (UUID viewer : viewers) {

            Player player = Bukkit.getPlayer(viewer);

            if (player == null) {
                viewers.remove(viewer);
                continue;
            }

            sendPacket(player, nmsPacket);
        }
    }

    protected ServerPlayer getNMSPlayer(Player bukkitPlayer) {
        return ((CraftPlayer) bukkitPlayer).getHandle();
    }
}
