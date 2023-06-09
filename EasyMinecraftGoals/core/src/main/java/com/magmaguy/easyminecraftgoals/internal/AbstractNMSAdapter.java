package com.magmaguy.easyminecraftgoals.internal;

import com.magmaguy.easyminecraftgoals.constants.OverridableWanderPriority;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

public interface AbstractNMSAdapter {

    /**
     * @param mob
     * @param speed
     * @param blockLocation
     */
    void move(Mob mob, double speed, Location blockLocation);

    /**
     * Makes a mob wander back to a point when it reaches a certain distance from that point, acting as a leash.
     * Please note that not all mobs in minecraft are able to pathfind - mobs like slimes can't. This leash can be
     * set to teleport them back, but if they're not set to teleport and they are set to walk back they will be unable
     * to do anything.
     * Pathfinding entities should extend Creature from the Spigot API. Non-creature entities are only allowable here
     * under the assumption that they will be set to teleport.
     *
     * @param livingEntity              Entity to leash
     * @param blockLocation             Location to leash to
     * @param maximumDistanceFromPoint  Maximum distance before the leash is applied
     * @param maxDurationTicks          Maximum duration of the navigation resulting form the leash
     * @param overridableWanderPriority Priority to apply to the goal
     * @return The instance of the WanderBackToPoint. Can be used in a builder pattern!
     */
    AbstractWanderBackToPoint wanderBackToPoint(LivingEntity livingEntity,
                                                Location blockLocation,
                                                double maximumDistanceFromPoint,
                                                int maxDurationTicks,
                                                OverridableWanderPriority overridableWanderPriority);
}
