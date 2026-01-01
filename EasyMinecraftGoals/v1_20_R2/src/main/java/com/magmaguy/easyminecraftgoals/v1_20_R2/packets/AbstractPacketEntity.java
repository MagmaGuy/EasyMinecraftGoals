package com.magmaguy.easyminecraftgoals.v1_20_R2.packets;

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
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class AbstractPacketEntity<T extends Entity> implements PacketEntityInterface {
    protected final T entity;
    private final Set<UUID> viewers = Sets.newConcurrentHashSet();
    private final List<Runnable> removeCallbacks = new LinkedList<>();
    protected boolean visible = true;

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

    protected AbstractPacketEntity(Location location) {
        this.entity = createEntity(location);
//        this.teleport(location); todo: might cause problems
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

    @SuppressWarnings("unchecked")
    @Override
    public <B extends org.bukkit.entity.Entity> B getBukkitEntity() {
        return (B) entity.getBukkitEntity();
    }

    @Override
    public void syncMetadata() {
        sendPacket(createEntityDataPacket());
    }

    @Override
    public void displayTo(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) displayTo(player);
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

        return new ClientboundSetEntityDataPacket(entity.getId(), dataValues);
    }

    public Packet<?> generateRemovePacket(){
        return new ClientboundRemoveEntitiesPacket(entity.getId());
    }

    //Entity destruction
    public void remove() {
        sendPacket(generateRemovePacket());
        removeCallbacks.forEach(Runnable::run);
    }

    //Visibility
    public Packet<?> generateSetVisiblePacket(boolean visible) {
        this.visible = visible;

        if (visible) {
            return createEntityDataPacket();
        } else {
            return new ClientboundRemoveEntitiesPacket(entity.getId());
        }
    }

    public void setVisible(boolean visible) {
        sendPacket(generateSetVisiblePacket(visible));
    }

    public Packet<?> generateHideFromPacket(UUID uuid){
        if (!viewers.contains(uuid)) {
            return null;
        }
        removeViewer(uuid);
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return null;
        return new ClientboundRemoveEntitiesPacket(entity.getId());
    }

    public void hideFrom(UUID uuid) {
        sendPacket(generateHideFromPacket(uuid));
    }

    public List<Packet<?>> generateDisplayToPackets(Player player) {
        List<Packet<?>> packets = new ArrayList<>();
        if (viewers.contains(player.getUniqueId())) {
            return packets;
        }
        addViewer(player.getUniqueId());

        packets.add(new ClientboundAddEntityPacket(
                entity.getId(),
                entity.getUUID(),
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                entity.getXRot(),
                entity.getYRot(),
                entity.getType(),
                0,
                new Vec3(0,0,0), 0));
        packets.add(createEntityDataPacket());
        return packets;
    }

    public void displayTo(Player player) {
        generateDisplayToPackets(player).forEach(packet -> sendPacket(player, packet));
    }

    //Teleports
    public void teleport(Location location) {
        entity.teleportTo(location.getX(), location.getY(), location.getZ());
        sendTeleportPacket();
    }

    protected void sendTeleportPacket() {
        sendPacket(generateTeleportPacket());
    }

    protected Packet<?> generateTeleportPacket() {
        return new ClientboundTeleportEntityPacket(entity);
    }

    //Move
    public Packet generateMovePacket(Location location){
        Location oldPos = getLocation();
        entity.setPos(location.getX(), location.getY(), location.getZ());
        entity.setYRot(location.getYaw());
        entity.setXRot(location.getPitch());

        Packet<?> movePacket = null;

        if (viewers.isEmpty()) {
            return movePacket;
        }

        Location newPos = getLocation();

        if (oldPos.getWorld() != newPos.getWorld()) {
            return generateTeleportPacket();
        }

        double deltaX = newPos.getX() - oldPos.getX();
        double deltaY = newPos.getY() - oldPos.getY();
        double deltaZ = newPos.getZ() - oldPos.getZ();

        if (deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > 16 * 16) {
            return generateTeleportPacket();
        }

        return new ClientboundMoveEntityPacket.PosRot(entity.getId(),
                (short) (deltaX * 4096),
                (short) (deltaY * 4096),
                (short) (deltaZ * 4096),
                (byte) (newPos.getYaw() * 256 / 360),
                (byte) (newPos.getPitch() * 256 / 360),
                true);
    }

    public void move(Location location) {
        sendPacket(generateMovePacket(location));
    }

    //Packet sending
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

    //Create packet bundle
    @Override
    public PacketBundle createPacketBundle() {
        return new PacketBundle();
    }

    protected ServerPlayer getNMSPlayer(Player bukkitPlayer) {
        return ((CraftPlayer) bukkitPlayer).getHandle();
    }
}
