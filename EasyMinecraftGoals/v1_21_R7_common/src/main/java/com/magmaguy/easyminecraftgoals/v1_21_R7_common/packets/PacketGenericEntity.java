package com.magmaguy.easyminecraftgoals.v1_21_R7_common.packets;

import com.magmaguy.easyminecraftgoals.v1_21_R7_common.CraftBukkitBridge;
import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
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
        // Use CraftBukkitBridge.getServerLevel directly since createNMSEntity requires ServerLevel
        return CraftBukkitBridge.createNMSEntity(bukkitType, CraftBukkitBridge.getServerLevel(location), location);
    }

    /**
     * Gets the Bukkit entity type this packet entity represents.
     */
    public EntityType getEntityType() {
        return bukkitType;
    }
}
