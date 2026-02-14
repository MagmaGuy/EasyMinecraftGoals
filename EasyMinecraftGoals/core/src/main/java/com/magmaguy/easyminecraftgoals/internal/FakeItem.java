package com.magmaguy.easyminecraftgoals.internal;

import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * A fake item entity that displays an item to players using packet-based entities.
 * Uses ItemDisplay for visual representation without creating a real pickable item.
 * <p>
 * This is ideal for visual effects like coin showers, loot previews, or any situation
 * where you want to show an item without it being pickable by hoppers or other plugins.
 */
public interface FakeItem {

    /**
     * Sets the item to display.
     *
     * @param itemStack The item to display
     */
    void setItemStack(ItemStack itemStack);

    /**
     * Gets the current displayed item.
     *
     * @return The current ItemStack
     */
    ItemStack getItemStack();

    /**
     * Sets whether the item display is visible.
     *
     * @param visible true to show the item, false to hide it
     */
    void setVisible(boolean visible);

    /**
     * Displays this fake item to a player.
     *
     * @param player The player to display to
     */
    void displayTo(Player player);

    /**
     * Displays this fake item to a player by UUID.
     *
     * @param uuid The UUID of the player to display to
     */
    void displayTo(UUID uuid);

    /**
     * Hides this fake item from a player.
     *
     * @param player The player to hide from
     */
    void hideFrom(Player player);

    /**
     * Hides this fake item from a player by UUID.
     *
     * @param uuid The UUID of the player to hide from
     */
    void hideFrom(UUID uuid);

    /**
     * Teleports this fake item to a new location.
     *
     * @param location The new location
     */
    void teleport(Location location);

    /**
     * Gets the current location of this fake item.
     *
     * @return The current location
     */
    Location getLocation();

    /**
     * Removes this fake item from all viewers and cleans up resources.
     */
    void remove();

    /**
     * Checks if this fake item has any viewers.
     *
     * @return true if there are viewers
     */
    boolean hasViewers();

    /**
     * Sets the scale of the item display.
     *
     * @param scale The scale multiplier (1.0 = normal size)
     */
    default void setScale(float scale) {
        // Default no-op for backwards compatibility
    }

    /**
     * Sets the billboard mode.
     *
     * @param billboard The billboard mode
     */
    default void setBillboard(Display.Billboard billboard) {
        // Default no-op for backwards compatibility
    }

    /**
     * Sets whether the item should glow.
     *
     * @param glowing true to enable glow effect
     */
    default void setGlowing(boolean glowing) {
        // Default no-op for backwards compatibility
    }

    /**
     * Sets the custom name displayed above the item.
     *
     * @param name The custom name (supports color codes)
     */
    default void setCustomName(String name) {
        // Default no-op for backwards compatibility
    }

    /**
     * Sets whether the custom name is visible.
     *
     * @param visible true to show the custom name
     */
    default void setCustomNameVisible(boolean visible) {
        // Default no-op for backwards compatibility
    }

    /**
     * Sets the Y-axis rotation (yaw) of the item display in degrees.
     *
     * @param yawDegrees The rotation around the Y-axis in degrees
     */
    default void setYawRotation(float yawDegrees) {
        // Default no-op for backwards compatibility
    }

    /**
     * Builder for creating FakeItem instances with custom styling.
     */
    interface Builder {
        /**
         * Sets the initial item to display.
         */
        Builder itemStack(ItemStack itemStack);

        /**
         * Sets the billboard mode.
         * Default is FIXED (no rotation).
         */
        Builder billboard(Display.Billboard billboard);

        /**
         * Sets the scale of the item.
         * Default is 1.0.
         */
        Builder scale(float scale);

        /**
         * Sets the view range multiplier.
         * Default is 1.0.
         */
        Builder viewRange(float range);

        /**
         * Sets whether the item should glow.
         * Default is false.
         */
        Builder glowing(boolean glowing);

        /**
         * Sets the custom name to display above the item.
         */
        Builder customName(String name);

        /**
         * Sets whether the custom name is visible.
         * Default is false.
         */
        Builder customNameVisible(boolean visible);

        /**
         * Builds the FakeItem at the specified location.
         *
         * @param location The spawn location
         * @return The created FakeItem
         */
        FakeItem build(Location location);
    }
}
