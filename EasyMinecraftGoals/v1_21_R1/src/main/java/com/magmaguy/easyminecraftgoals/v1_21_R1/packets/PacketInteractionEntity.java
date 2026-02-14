package com.magmaguy.easyminecraftgoals.v1_21_R1.packets;

import com.magmaguy.easyminecraftgoals.internal.PacketEntityInteractionManager;
import com.magmaguy.easyminecraftgoals.internal.PacketEntityInterface;
import net.minecraft.world.entity.Interaction;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

/**
 * A packet-only Interaction entity for handling player clicks.
 * This entity exists only in packets sent to clients - it's not added to the server world.
 * When players click on it, the interaction is intercepted and callbacks are fired.
 */
public class PacketInteractionEntity extends AbstractPacketEntity<Interaction>
        implements com.magmaguy.easyminecraftgoals.internal.PacketInteractionEntity {

    private BiConsumer<Player, PacketEntityInterface> rightClickCallback;
    private BiConsumer<Player, PacketEntityInterface> leftClickCallback;
    private float width = 1.0f;
    private float height = 1.0f;

    public PacketInteractionEntity(Location location) {
        super(location);
        // Register with the interaction manager for click handling
        PacketEntityInteractionManager.getInstance().register(getEntityId(), this);

        // Add remove callback to unregister
        addRemoveCallback(() -> {
            PacketEntityInteractionManager.getInstance().unregister(getEntityId());
        });
    }

    public PacketInteractionEntity(Location location, float width, float height) {
        this(location);
        setSize(width, height);
    }

    @Override
    protected Interaction createEntity(Location location) {
        // Use direct constructor like other packet entities
        Interaction interaction = new Interaction(
                net.minecraft.world.entity.EntityType.INTERACTION,
                getNMSLevel(location)
        );

        interaction.setPos(location.getX(), location.getY(), location.getZ());

        return interaction;
    }

    /**
     * Sets the size of the interaction hitbox.
     *
     * @param width  The width (and depth) of the hitbox
     * @param height The height of the hitbox
     */
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        entity.setWidth(width);
        entity.setHeight(height);
        // Sync metadata to update viewers
        syncMetadata();
    }

    /**
     * Gets the width of the interaction hitbox.
     */
    public float getWidth() {
        return width;
    }

    /**
     * Gets the height of the interaction hitbox.
     */
    public float getHeight() {
        return height;
    }

    @Override
    public int getEntityId() {
        return entity.getId();
    }

    @Override
    public void setRightClickCallback(BiConsumer<Player, PacketEntityInterface> callback) {
        this.rightClickCallback = callback;
    }

    @Override
    public void setLeftClickCallback(BiConsumer<Player, PacketEntityInterface> callback) {
        this.leftClickCallback = callback;
    }

    @Override
    public void handleInteraction(Player player, boolean isAttack) {
        if (isAttack) {
            if (leftClickCallback != null) {
                leftClickCallback.accept(player, this);
            }
        } else {
            if (rightClickCallback != null) {
                rightClickCallback.accept(player, this);
            }
        }
    }
}
