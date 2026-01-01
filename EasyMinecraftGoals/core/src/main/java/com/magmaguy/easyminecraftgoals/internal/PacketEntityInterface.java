package com.magmaguy.easyminecraftgoals.internal;

import org.bukkit.Location;

import java.util.UUID;

public interface PacketEntityInterface {
    void addRemoveCallback(Runnable callback);

    void displayTo(UUID uuid);

    void hideFrom(UUID uuid);

    void remove();

    void setVisible(boolean visible);

    Location getLocation();

    UUID getUniqueId();

    void teleport(Location location);

    void addViewer(UUID uuid);

    void removeViewer(UUID uuid);

    boolean hasViewers();

    AbstractPacketBundle createPacketBundle();

    /**
     * Syncs entity metadata to all viewers.
     * Call this after modifying the entity via getBukkitEntity().
     * Default implementation does nothing - override in implementations that support it.
     */
    default void syncMetadata() {
        // Default no-op for backwards compatibility
    }

    /**
     * Gets the underlying Bukkit entity for direct modification.
     * After modifying, call syncMetadata() to update viewers.
     * Default implementation throws - override in implementations that support it.
     *
     * @param <B> The Bukkit entity type to cast to
     * @return The Bukkit entity wrapper
     */
    default <B extends org.bukkit.entity.Entity> B getBukkitEntity() {
        throw new UnsupportedOperationException("getBukkitEntity is only supported in Minecraft 1.21.11+");
    }

}
