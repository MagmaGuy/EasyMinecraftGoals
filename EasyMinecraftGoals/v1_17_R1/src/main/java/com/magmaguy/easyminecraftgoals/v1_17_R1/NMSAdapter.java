package com.magmaguy.easyminecraftgoals.v1_17_R1;

import com.magmaguy.easyminecraftgoals.constants.OverridableWanderPriority;
import com.magmaguy.easyminecraftgoals.internal.AbstractNMSAdapter;
import com.magmaguy.easyminecraftgoals.internal.AbstractWanderBackToPoint;
import com.magmaguy.easyminecraftgoals.v1_17_R1.hitbox.Hitbox;
import com.magmaguy.easyminecraftgoals.v1_17_R1.move.Move;
import com.magmaguy.easyminecraftgoals.v1_17_R1.wanderbacktopoint.WanderBackToPointBehavior;
import com.magmaguy.easyminecraftgoals.v1_17_R1.wanderbacktopoint.WanderBackToPointGoal;
import net.minecraft.world.entity.PathfinderMob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class NMSAdapter extends com.magmaguy.easyminecraftgoals.NMSAdapter implements AbstractNMSAdapter {

    private PathfinderMob getPathfinderMob(Entity entity) {
        if (((CraftEntity) entity).getHandle() instanceof PathfinderMob pathfinderMob1)
            return pathfinderMob1;
        else
            return null;
    }

    @Override
    public boolean canReach(LivingEntity livingEntity, Location destination) {
        PathfinderMob pathfinderMob = getPathfinderMob(livingEntity);
        if (pathfinderMob == null) return false;
        return Move.canReach(pathfinderMob, destination);
    }

    @Override
    public boolean setCustomHitbox(Entity entity, float width, float height, boolean fixed) {
        if (entity == null) return false;
        return Hitbox.setCustomHitbox(((CraftEntity) entity).getHandle(), width, height, fixed);
    }

    @Override
    public boolean move(LivingEntity livingEntity, double speedModifier, Location location) {
        PathfinderMob pathfinderMob;
        if (((CraftLivingEntity) livingEntity).getHandle() instanceof PathfinderMob pathfinderMob1)
            pathfinderMob = pathfinderMob1;
        else
            pathfinderMob = null;
        if (!(((CraftLivingEntity) livingEntity).getHandle() instanceof net.minecraft.world.entity.Mob mob)) {
            Bukkit.getLogger().info("[EasyMinecraftPathfinding] Entity type " + livingEntity.getType() + " does not extend Mob and is therefore unable to have goals! It will not be able to pathfind.");
            return false;
        }
        return Move.simpleMove(pathfinderMob, speedModifier, location);
    }

    @Override
    public boolean forcedMove(LivingEntity livingEntity, double speedModifier, Location location) {
        if (!(((CraftLivingEntity) livingEntity).getHandle() instanceof net.minecraft.world.entity.Mob mob)) {
            Bukkit.getLogger().info("[EasyMinecraftPathfinding] Entity type " + livingEntity.getType() + " does not extend Mob and is therefore unable to have goals! It will not be able to pathfind.");
            return false;
        }
        return Move.forcedMove(mob, speedModifier, location);
    }

    @Override
    public void universalMove(LivingEntity livingEntity, double speedModifier, Location location) {
        if (!(((CraftLivingEntity) livingEntity).getHandle() instanceof net.minecraft.world.entity.Mob mob)) {
            Bukkit.getLogger().info("[EasyMinecraftPathfinding] Entity type " + livingEntity.getType() + " does not extend Mob and is therefore unable to have goals! It will not be able to pathfind.");
            return;
        }
        Move.universalMove(mob, speedModifier, location);
    }

    @Override
    public AbstractWanderBackToPoint wanderBackToPoint(LivingEntity livingEntity,
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
