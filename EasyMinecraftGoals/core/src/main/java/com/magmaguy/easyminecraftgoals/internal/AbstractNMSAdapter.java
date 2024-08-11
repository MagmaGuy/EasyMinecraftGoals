package com.magmaguy.easyminecraftgoals.internal;

import com.magmaguy.easyminecraftgoals.constants.OverridableWanderPriority;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public interface AbstractNMSAdapter {

    /**
     * @param livingEntity
     * @param speed
     * @param blockLocation
     */
    boolean move(LivingEntity livingEntity, double speed, Location blockLocation);

    /**
     * Wipes a mob's goals / brains (lobotomizes it) and makes it move to a location. The lobotomy is not reversible.
     * Use to make sure a mob with pathfinding goes to a specified location.
     *
     * @param livingEntity  The mob to move
     * @param speedModifier The speed the mob will move at
     * @param location      The final target location
     * @return Whether the movement order was successful
     */
    boolean forcedMove(LivingEntity livingEntity, double speedModifier, Location location);

    /**
     * Returns whether the given living entity can reach the given location using its default pathfinding
     * @param livingEntity Living entity that will be checked
     * @param destination Target location to be reached
     * @return Whether the location can be reached
     */
    boolean canReach(LivingEntity livingEntity, Location destination);

    void universalMove(LivingEntity livingEntity, double speedModifier, Location location);

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

    boolean setCustomHitbox(Entity entity, float width, float height, boolean fixed);

    float getBodyRotation(Entity entity);

    PacketModelEntity createPacketArmorStandEntity(Location location);
    PacketModelEntity createPacketDisplayEntity(Location location);
}
