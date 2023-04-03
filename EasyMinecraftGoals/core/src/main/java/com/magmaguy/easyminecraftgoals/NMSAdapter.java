package com.magmaguy.easyminecraftgoals;

import com.magmaguy.easyminecraftgoals.constants.OverridableWanderPriority;
import com.magmaguy.easyminecraftgoals.internal.AbstractNMSAdapter;
import com.magmaguy.easyminecraftgoals.internal.AbstractWanderBackToPoint;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class NMSAdapter implements AbstractNMSAdapter {
    @Override
    public void move(Mob mob, double speed, Location blockLocation) {
    }

    @Override
    public AbstractWanderBackToPoint returnToPointWhenOutOfCombatBehavior(LivingEntity livingEntity,
                                                                          Location blockLocation,
                                                                          double maximumDistanceFromPoint,
                                                                          int maxDurationTicks,
                                                                          OverridableWanderPriority overridableWanderPriority) {
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
    public AbstractWanderBackToPoint returnToPointWhenOutOfCombatBehavior(@NonNull LivingEntity livingEntity,
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
        return returnToPointWhenOutOfCombatBehavior(
                livingEntity,
                blockLocation,
                maximumDistanceFromPoint,
                maxDurationTicks,
                overridableWanderPriority);
    }


}
