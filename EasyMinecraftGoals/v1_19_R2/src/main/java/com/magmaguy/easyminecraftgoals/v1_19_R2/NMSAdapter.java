package com.magmaguy.easyminecraftgoals.v1_19_R2;

import com.magmaguy.easyminecraftgoals.constants.OverridableWanderPriority;
import com.magmaguy.easyminecraftgoals.internal.AbstractNMSAdapter;
import com.magmaguy.easyminecraftgoals.internal.AbstractWanderBackToPoint;
import com.magmaguy.easyminecraftgoals.v1_19_R2.wanderbacktopoint.WanderBackToPointBehavior;
import com.magmaguy.easyminecraftgoals.v1_19_R2.wanderbacktopoint.WanderBackToPointGoal;
import net.minecraft.world.entity.PathfinderMob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

public class NMSAdapter extends com.magmaguy.easyminecraftgoals.NMSAdapter implements AbstractNMSAdapter {
    @Override
    public void move(Mob mob, double speed, Location location) {
        //todo not done yet
    }

    @Override
    public AbstractWanderBackToPoint returnToPointWhenOutOfCombatBehavior(LivingEntity livingEntity,
                                                                          Location blockLocation,
                                                                          double maximumDistanceFromPoint,
                                                                          int maxDurationTicks,
                                                                          OverridableWanderPriority overridableWanderPriority) {
        PathfinderMob pathfinderMob;
        if (((CraftLivingEntity) livingEntity).getHandle() instanceof PathfinderMob pathfinderMob1)
            pathfinderMob = pathfinderMob1;
        else
            pathfinderMob = null;
        if (!(((CraftLivingEntity) livingEntity).getHandle() instanceof net.minecraft.world.entity.Mob mob)) {
            Bukkit.getLogger().info("[EasyMinecraftPathfinding] Entity type " + livingEntity.getType() + " does not extend Mob and is therefore unable to have goals! It will not be able to pathfind.");
            return null;
        }
        if (overridableWanderPriority.brain) return new WanderBackToPointBehavior(
                livingEntity,
                mob,
                blockLocation,
                maximumDistanceFromPoint,
                overridableWanderPriority.priority,
                maxDurationTicks);
        else return new WanderBackToPointGoal(
                mob,
                livingEntity,
                pathfinderMob,
                blockLocation,
                maximumDistanceFromPoint,
                overridableWanderPriority.priority,
                maxDurationTicks);
    }
}
