package com.magmaguy.easyminecraftgoals.internal;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * A fake text entity that displays text to players using packet-based entities.
 * Automatically uses TextDisplay for Java Edition players and ArmorStand for Bedrock Edition players.
 * <p>
 * TextDisplay features (like background color, alignment, billboard mode) are only applied
 * when the viewer is on Java Edition. Bedrock players see a simple armor stand with custom name.
 */
public interface FakeText {

    /**
     * Sets the text to display.
     *
     * @param text The text to display (supports MiniMessage format if available)
     */
    void setText(String text);

    /**
     * Gets the current display text.
     *
     * @return The current text
     */
    String getText();

    /**
     * Sets whether the text is visible.
     *
     * @param visible true to show the text, false to hide it
     */
    void setVisible(boolean visible);

    /**
     * Displays this fake text to a player.
     * Automatically chooses TextDisplay or ArmorStand based on the player's edition.
     *
     * @param player The player to display to
     */
    void displayTo(Player player);

    /**
     * Displays this fake text to a player by UUID.
     *
     * @param uuid The UUID of the player to display to
     */
    void displayTo(UUID uuid);

    /**
     * Hides this fake text from a player.
     *
     * @param player The player to hide from
     */
    void hideFrom(Player player);

    /**
     * Hides this fake text from a player by UUID.
     *
     * @param uuid The UUID of the player to hide from
     */
    void hideFrom(UUID uuid);

    /**
     * Teleports this fake text to a new location.
     *
     * @param location The new location
     */
    void teleport(Location location);

    /**
     * Gets the current location of this fake text.
     *
     * @return The current location
     */
    Location getLocation();

    /**
     * Removes this fake text from all viewers and cleans up resources.
     */
    void remove();

    /**
     * Checks if this fake text has any viewers.
     *
     * @return true if there are viewers
     */
    boolean hasViewers();

    /**
     * Sets the text opacity (TextDisplay only).
     * For animated fade effects.
     *
     * @param opacity 0-255, where 255 is fully opaque
     */
    default void setTextOpacity(byte opacity) {
        // Default no-op for backwards compatibility
    }

    /**
     * Sets the scale of the text (TextDisplay only).
     * For animated scale effects.
     *
     * @param scale The scale multiplier (1.0 = normal size)
     */
    default void setScale(float scale) {
        // Default no-op for backwards compatibility
    }

    /**
     * Sets the background color (TextDisplay only).
     *
     * @param color The background color
     */
    default void setBackgroundColor(Color color) {
        // Default no-op for backwards compatibility
    }

    /**
     * Sets the background color with alpha (TextDisplay only).
     *
     * @param argb ARGB color value
     */
    default void setBackgroundColor(int argb) {
        // Default no-op for backwards compatibility
    }

    /**
     * Sets whether the text has shadow (TextDisplay only).
     *
     * @param shadow true to enable shadow
     */
    default void setShadowed(boolean shadow) {
        // Default no-op for backwards compatibility
    }

    /**
     * Sets whether the text is see-through (TextDisplay only).
     *
     * @param seeThrough true to enable see-through
     */
    default void setSeeThrough(boolean seeThrough) {
        // Default no-op for backwards compatibility
    }

    /**
     * Sets the billboard mode (TextDisplay only).
     *
     * @param billboard The billboard mode
     */
    default void setBillboard(Display.Billboard billboard) {
        // Default no-op for backwards compatibility
    }

    /**
     * Makes this fake text ride on top of an entity.
     * The client will handle positioning automatically - no need to teleport.
     * Does NOT enable automatic visibility tracking - use attachTo() for that.
     *
     * @param vehicle The entity to mount (e.g., a Player)
     */
    default void mountTo(org.bukkit.entity.Entity vehicle) {
        // Default no-op for backwards compatibility with older versions
    }

    /**
     * Dismounts this fake text from any vehicle it's currently riding.
     */
    default void dismount() {
        // Default no-op for backwards compatibility with older versions
    }

    /**
     * Attaches this fake text to an entity with automatic visibility tracking.
     * This will:
     * - Mount the text to the entity
     * - Register with PacketEntityTracker for automatic show/hide based on distance
     * - Handle world changes, respawns, and other edge cases automatically
     * <p>
     * Use this instead of mountTo() for a fully managed experience.
     *
     * @param vehicle The entity to attach to (e.g., a Player)
     */
    default void attachTo(org.bukkit.entity.Entity vehicle) {
        // Default no-op for backwards compatibility with older versions
    }

    /**
     * Detaches this fake text from its vehicle and stops automatic tracking.
     * Hides from all current viewers.
     */
    default void detach() {
        // Default no-op for backwards compatibility with older versions
    }

    /**
     * Gets the vehicle entity this fake text is attached to, if any.
     *
     * @return The vehicle entity, or null if not attached
     */
    default org.bukkit.entity.Entity getVehicle() {
        return null;
    }

    /**
     * Checks if this fake text is using automatic tracking via attachTo().
     *
     * @return true if auto-tracked
     */
    default boolean isAutoTracked() {
        return false;
    }

    /**
     * Builder for creating FakeText instances with custom styling.
     * Styling options only affect TextDisplay (Java Edition players).
     */
    interface Builder {
        /**
         * Sets the initial text.
         */
        Builder text(String text);

        /**
         * Sets the background color (TextDisplay only).
         * Default is transparent (no background).
         */
        Builder backgroundColor(Color color);

        /**
         * Sets the background color with alpha (TextDisplay only).
         *
         * @param argb ARGB color value
         */
        Builder backgroundColor(int argb);

        /**
         * Sets the text opacity (TextDisplay only).
         *
         * @param opacity 0-255, where 255 is fully opaque
         */
        Builder textOpacity(byte opacity);

        /**
         * Sets the billboard mode (TextDisplay only).
         * Default is CENTER (always faces the player).
         */
        Builder billboard(Display.Billboard billboard);

        /**
         * Sets the text alignment (TextDisplay only).
         * Default is CENTER.
         */
        Builder alignment(TextAlignment alignment);

        /**
         * Sets whether the text has shadow (TextDisplay only).
         * Default is false.
         */
        Builder shadow(boolean shadow);

        /**
         * Sets whether the text is see-through (TextDisplay only).
         * Default is false.
         */
        Builder seeThrough(boolean seeThrough);

        /**
         * Sets the line width before wrapping (TextDisplay only).
         * Default is 200.
         */
        Builder lineWidth(int width);

        /**
         * Sets the view range multiplier (TextDisplay only).
         * Default is 1.0.
         */
        Builder viewRange(float range);

        /**
         * Sets the scale of the text (TextDisplay only).
         * Default is 1.0.
         */
        Builder scale(float scale);

        /**
         * Sets the translation offset of the text (TextDisplay only).
         * This offsets the text from its spawn location.
         * Default is (0, 0, 0).
         */
        Builder translation(float x, float y, float z);

        /**
         * Builds the FakeText at the specified location.
         *
         * @param location The spawn location
         * @return The created FakeText
         */
        FakeText build(Location location);
    }

    /**
     * Text alignment options for TextDisplay.
     */
    enum TextAlignment {
        CENTER,
        LEFT,
        RIGHT
    }
}
