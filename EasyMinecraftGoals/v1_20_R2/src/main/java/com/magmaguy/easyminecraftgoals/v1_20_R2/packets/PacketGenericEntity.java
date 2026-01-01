package com.magmaguy.easyminecraftgoals.v1_20_R2.packets;

import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity;
import org.bukkit.entity.EntityType;

/**
 * A generic packet entity that can be any entity type.
 * Use getBukkitEntity() to cast to the specific Bukkit entity type and modify properties.
 * Call syncMetadata() after modifications to update all viewers.
 */
public class PacketGenericEntity extends AbstractPacketEntity<Entity> {

    private final EntityType bukkitType;

    public PacketGenericEntity(EntityType entityType, Location location) {
        super(location);
        this.bukkitType = entityType;
    }

    @Override
    protected Entity createEntity(Location location) {
        CraftWorld craftWorld = (CraftWorld) location.getWorld();
        Class<? extends org.bukkit.entity.Entity> entityClass = bukkitType.getEntityClass();
        if (entityClass == null) {
            throw new RuntimeException("No entity class for type: " + bukkitType);
        }

        Entity nmsEntity = craftWorld.createEntity(location, entityClass);

        nmsEntity.setPos(location.getX(), location.getY(), location.getZ());
        if (location.getYaw() != 0) nmsEntity.setYRot(location.getYaw());
        if (location.getPitch() != 0) nmsEntity.setXRot(location.getPitch());

        return nmsEntity;
    }

    /**
     * Gets the Bukkit entity type this packet entity represents.
     */
    public EntityType getEntityType() {
        return bukkitType;
    }
}
