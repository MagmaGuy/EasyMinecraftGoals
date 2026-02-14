package com.magmaguy.easyminecraftgoals.internal;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global tracker for packet entities.
 * Runs a tick task to manage visibility based on player distance.
 * Handles player join, quit, respawn, and world changes.
 */
public class PacketEntityTracker {

    private static PacketEntityTracker instance;

    private final Set<TrackedPacketEntity> trackedEntities = ConcurrentHashMap.newKeySet();
    private BukkitTask tickTask;
    private Plugin plugin;
    private long tickCounter = 0;

    // Update visibility every 20 ticks (1 second)
    private static final int VISIBILITY_UPDATE_INTERVAL = 20;

    // Default tracking range (blocks) - uses squared distance for performance
    private double trackingRangeSquared = 64 * 64; // 64 blocks default

    private PacketEntityTracker() {
    }

    public static PacketEntityTracker getInstance() {
        if (instance == null) {
            instance = new PacketEntityTracker();
        }
        return instance;
    }

    /**
     * Initializes the tracker with the plugin instance.
     * Starts the tick task and registers event listeners.
     *
     * @param plugin The plugin instance
     */
    public void initialize(Plugin plugin) {
        if (this.plugin != null) {
            return; // Already initialized
        }

        this.plugin = plugin;

        // Try to get server's entity tracking range
        updateTrackingRange();

        // Register event listener
        Bukkit.getPluginManager().registerEvents(new PacketEntityEventListener(this), plugin);

        // Start the tick task - runs every tick
        tickTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    /**
     * Shuts down the tracker.
     * Stops the tick task and removes all tracked entities.
     */
    public void shutdown() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }

        // Remove all tracked entities
        for (TrackedPacketEntity entity : trackedEntities) {
            for (UUID viewerUUID : entity.getCurrentViewers()) {
                Player viewer = Bukkit.getPlayer(viewerUUID);
                if (viewer != null) {
                    entity.hideFromPlayer(viewer);
                }
            }
        }
        trackedEntities.clear();

        plugin = null;
    }

    /**
     * Registers a packet entity for tracking.
     *
     * @param entity The entity to track
     */
    public void register(TrackedPacketEntity entity) {
        trackedEntities.add(entity);
    }

    /**
     * Unregisters a packet entity from tracking.
     *
     * @param entity The entity to unregister
     */
    public void unregister(TrackedPacketEntity entity) {
        trackedEntities.remove(entity);
    }

    /**
     * Called every tick by the scheduler.
     */
    private void tick() {
        tickCounter++;

        // Only update visibility every VISIBILITY_UPDATE_INTERVAL ticks
        if (tickCounter % VISIBILITY_UPDATE_INTERVAL != 0) {
            return;
        }

        updateVisibility();
    }

    /**
     * Updates visibility for all tracked entities.
     */
    private void updateVisibility() {
        Iterator<TrackedPacketEntity> iterator = trackedEntities.iterator();

        while (iterator.hasNext()) {
            TrackedPacketEntity entity = iterator.next();

            // Check if entity is still valid
            if (!entity.isValid()) {
                iterator.remove();
                continue;
            }

            Location entityLocation = entity.getTrackingLocation();
            if (entityLocation == null) {
                continue;
            }

            World entityWorld = entity.getWorld();
            if (entityWorld == null) {
                continue;
            }

            // Check each online player
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateVisibilityForPlayer(entity, entityLocation, entityWorld, player);
            }
        }
    }

    /**
     * Updates visibility of a tracked entity for a specific player.
     */
    private void updateVisibilityForPlayer(TrackedPacketEntity entity, Location entityLocation,
                                           World entityWorld, Player player) {
        boolean shouldBeVisible = false;

        // Same world check
        if (player.getWorld().equals(entityWorld)) {
            // Distance check using squared distance for performance
            double distanceSquared = player.getLocation().distanceSquared(entityLocation);
            shouldBeVisible = distanceSquared <= trackingRangeSquared;
        }

        boolean isCurrentlyVisible = entity.isVisibleTo(player);

        if (shouldBeVisible && !isCurrentlyVisible) {
            // Show to player
            entity.showToPlayer(player);
        } else if (!shouldBeVisible && isCurrentlyVisible) {
            // Hide from player
            entity.hideFromPlayer(player);
        }
    }

    /**
     * Called when a player joins the server.
     * Checks all tracked entities and shows those in range.
     *
     * @param player The joining player
     */
    void onPlayerJoin(Player player) {
        // Delay by 1 tick to ensure player is fully loaded
        if (plugin != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                updateVisibilityForAllEntities(player);
            }, 1L);
        }
    }

    /**
     * Called when a player quits the server.
     * Removes the player from all viewer lists.
     *
     * @param player The quitting player
     */
    void onPlayerQuit(Player player) {
        for (TrackedPacketEntity entity : trackedEntities) {
            if (entity.isVisibleTo(player)) {
                entity.hideFromPlayer(player);
            }
        }
    }

    /**
     * Called when a player respawns.
     * Re-checks visibility after a short delay.
     *
     * @param player The respawning player
     */
    void onPlayerRespawn(Player player) {
        // Delay by 1 tick to ensure respawn is complete
        if (plugin != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;

                // First, update visibility for entities the player should see
                updateVisibilityForAllEntities(player);

                // Also check if this player is a vehicle for any tracked entities
                // and remount them if needed
                for (TrackedPacketEntity entity : trackedEntities) {
                    Entity vehicle = entity.getVehicle();
                    if (vehicle != null && vehicle.getUniqueId().equals(player.getUniqueId())) {
                        entity.remount();
                    }
                }
            }, 1L);
        }
    }

    /**
     * Called when a player changes worlds.
     * Hides entities from old world, shows entities from new world.
     *
     * @param player   The player who changed worlds
     * @param fromWorld The world they came from
     */
    void onPlayerChangedWorld(Player player, World fromWorld) {
        // First, handle entities visible to this player (hide old world, show new world)
        // This doesn't need a delay
        World toWorld = player.getWorld();

        for (TrackedPacketEntity entity : trackedEntities) {
            World entityWorld = entity.getWorld();
            if (entityWorld == null) continue;

            // If entity was in old world and visible, hide it
            if (entityWorld.equals(fromWorld) && entity.isVisibleTo(player)) {
                entity.hideFromPlayer(player);
            }
        }

        // Check if this player is a vehicle for any tracked entities
        // If so, we need to hide from all current viewers and re-show after a delay
        for (TrackedPacketEntity entity : trackedEntities) {
            Entity vehicle = entity.getVehicle();
            if (vehicle != null && vehicle.getUniqueId().equals(player.getUniqueId())) {
                // The vehicle (player) changed worlds
                // Hide from ALL current viewers - they were seeing it in old world context
                for (UUID viewerUUID : new HashSet<>(entity.getCurrentViewers())) {
                    Player viewer = Bukkit.getPlayer(viewerUUID);
                    if (viewer != null) {
                        entity.hideFromPlayer(viewer);
                    }
                }
            }
        }

        // Delay the re-show to ensure world change is complete
        if (plugin != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;

                World newWorld = player.getWorld();

                // Show entities in new world to this player
                for (TrackedPacketEntity entity : trackedEntities) {
                    World entityWorld = entity.getWorld();
                    if (entityWorld == null) continue;

                    if (entityWorld.equals(newWorld)) {
                        Location entityLocation = entity.getTrackingLocation();
                        if (entityLocation != null) {
                            double distanceSquared = player.getLocation().distanceSquared(entityLocation);
                            if (distanceSquared <= trackingRangeSquared && !entity.isVisibleTo(player)) {
                                entity.showToPlayer(player);
                            }
                        }
                    }
                }

                // Re-show entities attached to this player to nearby players
                for (TrackedPacketEntity entity : trackedEntities) {
                    Entity vehicle = entity.getVehicle();
                    if (vehicle != null && vehicle.getUniqueId().equals(player.getUniqueId())) {
                        // Show to players in new world (within range)
                        for (Player otherPlayer : newWorld.getPlayers()) {
                            if (!entity.isVisibleTo(otherPlayer)) {
                                Location entityLocation = entity.getTrackingLocation();
                                if (entityLocation != null) {
                                    double distanceSquared = otherPlayer.getLocation().distanceSquared(entityLocation);
                                    if (distanceSquared <= trackingRangeSquared) {
                                        entity.showToPlayer(otherPlayer);
                                    }
                                }
                            }
                        }
                        // Remount after showing
                        entity.remount();
                    }
                }
            }, 1L);
        }
    }

    /**
     * Updates visibility of all tracked entities for a specific player.
     */
    private void updateVisibilityForAllEntities(Player player) {
        for (TrackedPacketEntity entity : trackedEntities) {
            if (!entity.isValid()) continue;

            Location entityLocation = entity.getTrackingLocation();
            if (entityLocation == null) continue;

            World entityWorld = entity.getWorld();
            if (entityWorld == null) continue;

            updateVisibilityForPlayer(entity, entityLocation, entityWorld, player);
        }
    }

    /**
     * Updates the tracking range from server settings.
     */
    private void updateTrackingRange() {
        try {
            // Try to get entity tracking range from spigot config
            // Default is usually 48-64 blocks
            int range = Bukkit.getViewDistance() * 16; // chunks to blocks
            trackingRangeSquared = range * range;
        } catch (Exception e) {
            // Fall back to default
            trackingRangeSquared = 64 * 64;
        }
    }

    /**
     * Gets the current tracking range in blocks.
     */
    public double getTrackingRange() {
        return Math.sqrt(trackingRangeSquared);
    }

    /**
     * Sets the tracking range in blocks.
     */
    public void setTrackingRange(double range) {
        this.trackingRangeSquared = range * range;
    }
}
