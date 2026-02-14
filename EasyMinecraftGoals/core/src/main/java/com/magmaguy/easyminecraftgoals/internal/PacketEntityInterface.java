package com.magmaguy.easyminecraftgoals.internal;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.BiConsumer;

public interface PacketEntityInterface {
    void addRemoveCallback(Runnable callback);

    void displayTo(UUID uuid);

    void hideFrom(UUID uuid);

    void remove();

    void setVisible(boolean visible);

    Location getLocation();

    UUID getUniqueId();

    void teleport(Location location);

    /**
     * Makes this packet entity ride another entity (the vehicle).
     * Sends mount packets to all viewers so the client handles positioning.
     *
     * @param vehicleEntityId The entity ID of the vehicle to mount
     */
    default void mountTo(int vehicleEntityId) {
        // Default no-op for backwards compatibility
    }

    /**
     * Dismounts this packet entity from any vehicle.
     * Sends dismount packets to all viewers.
     */
    default void dismount() {
        // Default no-op for backwards compatibility
    }

    /**
     * Gets the entity ID of this packet entity.
     *
     * @return The entity ID
     */
    default int getEntityId() {
        return -1;
    }

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

    /**
     * Sets a callback to be invoked when a player right-clicks (interacts with) this packet entity.
     * The callback receives the player who clicked and this packet entity.
     *
     * @param callback The callback to invoke on right-click, or null to remove
     */
    default void setRightClickCallback(BiConsumer<Player, PacketEntityInterface> callback) {
        // Default no-op for backwards compatibility
    }

    /**
     * Sets a callback to be invoked when a player left-clicks (attacks) this packet entity.
     * The callback receives the player who clicked and this packet entity.
     *
     * @param callback The callback to invoke on left-click/attack, or null to remove
     */
    default void setLeftClickCallback(BiConsumer<Player, PacketEntityInterface> callback) {
        // Default no-op for backwards compatibility
    }

    /**
     * Called internally when a player interacts with this packet entity.
     * Should not be called directly - use setRightClickCallback instead.
     *
     * @param player The player who interacted
     * @param isAttack True if this was an attack (left-click), false if interact (right-click)
     */
    default void handleInteraction(Player player, boolean isAttack) {
        // Default no-op for backwards compatibility
    }

}
