package com.magmaguy.easyminecraftgoals.internal;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages packet entity interactions by tracking entity IDs and dispatching interaction events.
 * This allows packet-only entities to receive click events from players.
 */
public class PacketEntityInteractionManager {

    private static PacketEntityInteractionManager instance;

    // Maps entity ID to packet entity for quick lookup when interaction packets arrive
    private final Map<Integer, PacketEntityInterface> entityIdMap = new ConcurrentHashMap<>();

    private PacketEntityInteractionManager() {
    }

    public static PacketEntityInteractionManager getInstance() {
        if (instance == null) {
            instance = new PacketEntityInteractionManager();
        }
        return instance;
    }

    /**
     * Registers a packet entity for interaction tracking.
     *
     * @param entityId The entity ID (from NMS entity)
     * @param entity   The packet entity
     */
    public void register(int entityId, PacketEntityInterface entity) {
        entityIdMap.put(entityId, entity);
    }

    /**
     * Unregisters a packet entity from interaction tracking.
     *
     * @param entityId The entity ID to unregister
     */
    public void unregister(int entityId) {
        entityIdMap.remove(entityId);
    }

    /**
     * Gets a packet entity by its entity ID.
     *
     * @param entityId The entity ID
     * @return The packet entity, or null if not found
     */
    public PacketEntityInterface getByEntityId(int entityId) {
        return entityIdMap.get(entityId);
    }

    /**
     * Handles an interaction packet from a player.
     * Called by the packet interceptor when a ServerboundInteractPacket is received.
     *
     * @param player   The player who sent the interaction
     * @param entityId The entity ID being interacted with
     * @param isAttack True if this was an attack action, false if interact
     * @return True if the interaction was handled by a packet entity, false otherwise
     */
    public boolean handleInteraction(Player player, int entityId, boolean isAttack) {
        PacketEntityInterface entity = entityIdMap.get(entityId);
        if (entity == null) {
            return false;
        }

        entity.handleInteraction(player, isAttack);
        return true;
    }

    /**
     * Clears all registered entities.
     * Called on shutdown.
     */
    public void shutdown() {
        entityIdMap.clear();
    }
}
