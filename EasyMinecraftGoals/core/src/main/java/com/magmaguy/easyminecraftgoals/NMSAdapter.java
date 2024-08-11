package com.magmaguy.easyminecraftgoals;

import com.magmaguy.easyminecraftgoals.constants.OverridableWanderPriority;
import com.magmaguy.easyminecraftgoals.internal.AbstractNMSAdapter;
import com.magmaguy.easyminecraftgoals.internal.AbstractWanderBackToPoint;
import com.magmaguy.easyminecraftgoals.internal.PacketModelEntity;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class NMSAdapter implements AbstractNMSAdapter {
    /**
     * Simply makes an entity move to a point.
     *
     * @param livingEntity   The entity to move
     * @param speedModifier  The speed modifier. Set 1.0 for the base movement speed attribute.
     * @param targetLocation The target location
     * @return Whether the objective is reachable.
     */
    @Override
    public boolean move(LivingEntity livingEntity, double speedModifier, Location targetLocation) {
        //Gets overriden by the correct adapter
        return false;
    }

    @Override
    public boolean forcedMove(LivingEntity livingEntity, double speedModifier, Location location) {
        //Gets overriden by the correct adapter
        return false;
    }

    /**
     * Pathfinding method which will move an entity to a point with a complete disregard for any pathfinding. This means
     * that no pathfinding will be done by Minecraft - the entity can get stuck on blocks, won't be able to jump or really
     * hardly any obstacle. Highly efficient, highly predictable.
     *
     * @param livingEntity  Entity to move
     * @param speedModifier Speed modifier
     * @param location      Location to move to
     */
    @Override
    public void universalMove(LivingEntity livingEntity, double speedModifier, Location location) {
        //Gets overriden by the correct adapter
    }


    @Override
    public AbstractWanderBackToPoint wanderBackToPoint(LivingEntity livingEntity,
                                                       Location blockLocation,
                                                       double maximumDistanceFromPoint,
                                                       int maxDurationTicks,
                                                       OverridableWanderPriority overridableWanderPriority) {
        //Gets overriden by the correct adapter
        return null;
    }

    /**
     * Sets a custom hitbox size for an entity
     * @param entity The entity whose hitbox is about to get resized
     * @param width Width of the hitbox. Sets the X and Z axis at the same time, that's a Minecraft limitation
     * @param height The height of the hitbox, has to be lower than 64.
     * @param fixed Whether the hitbox can scale
     * @return
     */
    @Override
    public boolean setCustomHitbox(Entity entity, float width, float height, boolean fixed) {
        //Gets overriden by the correct adapter
        return false;
    }

    @Override
    public float getBodyRotation(Entity entity) {
        //Gets overriden by the correct adapter
        return 0;
    }

    @Override
    public PacketModelEntity createPacketArmorStandEntity(Location location) {
        //Gets overriden by the correct adapter
        return null;
    }

    @Override
    public PacketModelEntity createPacketDisplayEntity(Location location) {
        //Gets overriden by the correct adapter
        return null;
    }

    /**
     * Makes a mob wander back to a point when it reaches a certain distance from that point, acting as a leash.
     * Please note that not all mobs in minecraft are able to pathfind - mobs like slimes can't. This leash can be
     * set to teleport them back, but if they're not set to teleport and they are set to walk back they will be unable
     * to do anything.
     * Pathfinding entities should extend Creature from the Spigot API. Non-creature entities are only allowable here
     * under the assumption that they will be set to teleport.
     *
     * @param livingEntity             Entity to leash
     * @param blockLocation            Location to leash to
     * @param maximumDistanceFromPoint Maximum distance before the leash is applied
     * @param maxDurationTicks         Maximum duration of the navigation resulting form the leash
     * @return The instance of the WanderBackToPoint. Can be used in a builder pattern!
     */
    @Nullable
    public AbstractWanderBackToPoint wanderBackToPoint(@NonNull LivingEntity livingEntity,
                                                       @NonNull Location blockLocation,
                                                       double maximumDistanceFromPoint,
                                                       int maxDurationTicks) {
        OverridableWanderPriority overridableWanderPriority;
        try {
            overridableWanderPriority = OverridableWanderPriority.valueOf(livingEntity.getType().name());
        } catch (Exception ex) {
            NMSManager.pluginProvider.getLogger().warning("[EasyMinecraftPathfinding] Attempted to assign return point to entity type " + livingEntity.getType().name() + " which is not a currently accepted entity type!");
            return null;
        }
        return wanderBackToPoint(
                livingEntity,
                blockLocation,
                maximumDistanceFromPoint,
                maxDurationTicks,
                overridableWanderPriority);
    }

    @Override
    public boolean canReach(LivingEntity livingEntity, Location location){
        //Gets overriden by the correct adapter
        return false;
    }

}
