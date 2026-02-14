package com.magmaguy.easyminecraftgoals.internal;

import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

/**
 * Interface for packet-only Interaction entities that can receive player clicks.
 * These entities exist only in packets sent to clients - they are not added to the server world.
 * When players click on them, callbacks are fired.
 */
public interface PacketInteractionEntity extends PacketEntityInterface {

    /**
     * Sets the size of the interaction hitbox.
     *
     * @param width  The width (and depth) of the hitbox
     * @param height The height of the hitbox
     */
    void setSize(float width, float height);

    /**
     * Gets the width of the interaction hitbox.
     */
    float getWidth();

    /**
     * Gets the height of the interaction hitbox.
     */
    float getHeight();

    /**
     * Sets a callback to be invoked when a player right-clicks this interaction entity.
     *
     * @param callback The callback receiving the player and this entity, or null to remove
     */
    void setRightClickCallback(BiConsumer<Player, PacketEntityInterface> callback);

    /**
     * Sets a callback to be invoked when a player left-clicks (attacks) this interaction entity.
     *
     * @param callback The callback receiving the player and this entity, or null to remove
     */
    void setLeftClickCallback(BiConsumer<Player, PacketEntityInterface> callback);
}
