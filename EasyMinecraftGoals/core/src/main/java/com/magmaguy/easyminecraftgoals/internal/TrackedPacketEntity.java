package com.magmaguy.easyminecraftgoals.internal;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

/**
 * Interface for packet entities that want automatic visibility management.
 * Entities implementing this interface can register with PacketEntityTracker
 * to automatically show/hide based on player distance.
 */
public interface TrackedPacketEntity {

    /**
     * Gets the location used for tracking distance.
     * If attached to a vehicle, this should return the vehicle's location.
     *
     * @return The tracking location
     */
    Location getTrackingLocation();

    /**
     * Gets the world this entity is in.
     *
     * @return The world
     */
    World getWorld();

    /**
     * Shows this entity to a player.
     * Should send spawn packets and any additional packets (mount, metadata, etc.)
     *
     * @param player The player to show to
     */
    void showToPlayer(Player player);

    /**
     * Hides this entity from a player.
     * Should send remove packets.
     *
     * @param player The player to hide from
     */
    void hideFromPlayer(Player player);

    /**
     * Checks if this entity is currently visible to a player.
     *
     * @param player The player to check
     * @return true if visible
     */
    boolean isVisibleTo(Player player);

    /**
     * Gets all players who can currently see this entity.
     *
     * @return Set of viewer UUIDs
     */
    Set<UUID> getCurrentViewers();

    /**
     * Checks if this entity is still valid and should be tracked.
     *
     * @return true if valid
     */
    boolean isValid();

    /**
     * Gets the unique identifier for this entity.
     *
     * @return The UUID
     */
    UUID getUniqueId();

    /**
     * Gets the vehicle entity this is attached to, if any.
     *
     * @return The vehicle entity, or null if not attached
     */
    Entity getVehicle();

    /**
     * Called when the entity needs to remount to its vehicle.
     * This is used after world changes, respawns, etc.
     */
    void remount();
}
