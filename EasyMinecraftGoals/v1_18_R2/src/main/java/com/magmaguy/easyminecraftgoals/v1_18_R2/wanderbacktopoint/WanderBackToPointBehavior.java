package com.magmaguy.easyminecraftgoals.v1_18_R2.wanderbacktopoint;

import com.google.common.collect.ImmutableList;
import com.magmaguy.easyminecraftgoals.events.WanderBackToPointEndEvent;
import com.magmaguy.easyminecraftgoals.events.WanderBackToPointStartEvent;
import com.magmaguy.easyminecraftgoals.internal.AbstractWanderBackToPoint;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Map;

public class WanderBackToPointBehavior extends Behavior<LivingEntity> implements AbstractWanderBackToPoint {

    private Vec3 returnLocation;
    private double maximumDistanceFromPoint;
    private long lastTime;
    private org.bukkit.entity.LivingEntity livingEntity;
    private Mob mob;
    private int priority;
    private int maxDurationTicks;

    private float speed;
    private int stopReturnDistance = 0;
    //Note: This assumes 20 tps
    private int goalRefreshCooldownTicks = 3 * 20;
    private boolean hardObjective = false;
    private boolean teleportOnFail = false;
    private boolean startWithCooldown = false;

    public WanderBackToPointBehavior(org.bukkit.entity.LivingEntity livingEntity,
                                     Mob mob,
                                     Location location,
                                     double maximumDistanceFromPoint,
                                     int priority,
                                     int maxDurationTicks) {
        //Memory status REGISTERED means that the module will always apply, overriding existing values
        super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED), 0, maxDurationTicks);
        this.livingEntity = livingEntity;
        this.mob = mob;
        this.returnLocation = new Vec3(location.getX(), location.getY(), location.getZ());
        this.maximumDistanceFromPoint = maximumDistanceFromPoint;
        this.maxDurationTicks = maxDurationTicks;
        this.priority = priority;
        this.lastTime = 0;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel var0, LivingEntity nmsLivingEntity) {
        if (!hardObjective && mob.getTarget() instanceof Player) {
            updateCooldown();
            return false;
        }
        if (lastTime < System.currentTimeMillis() + 50L * goalRefreshCooldownTicks) return false;
        updateCooldown();
        Vec3 targetLocation = new Vec3(nmsLivingEntity.getX(), nmsLivingEntity.getY(), nmsLivingEntity.getZ());
        if (targetLocation.distanceToSqr(returnLocation) < maximumDistanceFromPoint * maximumDistanceFromPoint)
            return false;
        WanderBackToPointStartEvent wanderBackToPointStartEvent = new WanderBackToPointStartEvent(hardObjective, livingEntity);
        Bukkit.getPluginManager().callEvent(wanderBackToPointStartEvent);
        if (wanderBackToPointStartEvent.isCancelled()) return false;
        if (teleportOnFail) {
            Path path = ((PathfinderMob) nmsLivingEntity).getNavigation().createPath(returnLocation.x, returnLocation.y, returnLocation.z, stopReturnDistance);
            if (path == null || !path.canReach()) {
                nmsLivingEntity.teleportTo(returnLocation.x, returnLocation.y, returnLocation.z);
                WanderBackToPointEndEvent wanderBackToPointEndEvent = new WanderBackToPointEndEvent(hardObjective, livingEntity);
                Bukkit.getPluginManager().callEvent(wanderBackToPointEndEvent);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void start(ServerLevel var0, LivingEntity var1, long var2) {
        setWalkAndLookTarget(var1);
    }

    @Override
    protected void stop(ServerLevel var0, LivingEntity var1, long var2) {
        WanderBackToPointEndEvent wanderBackToPointEndEvent = new WanderBackToPointEndEvent(hardObjective, livingEntity);
        Bukkit.getPluginManager().callEvent(wanderBackToPointEndEvent);
        updateCooldown();
    }

    @Override
    protected boolean canStillUse(ServerLevel var0, LivingEntity var1, long var2) {
        if (!hardObjective && mob.getTarget() instanceof Player) return false;
        return !mob.getNavigation().isDone();
    }

    private void setWalkAndLookTarget(LivingEntity livingEntity) {
        Brain<?> brain = livingEntity.getBrain();
        WalkTarget walkTarget = new WalkTarget(returnLocation, speed, stopReturnDistance);
        brain.setMemory(MemoryModuleType.WALK_TARGET, walkTarget);
    }

    //AbstractWanderBackToPoint start

    @Override
    public AbstractWanderBackToPoint setSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    @Override
    public AbstractWanderBackToPoint setStopReturnDistance(int distance) {
        this.stopReturnDistance = distance;
        return this;
    }

    @Override
    public AbstractWanderBackToPoint setGoalRefreshCooldownTicks(int ticks) {
        this.goalRefreshCooldownTicks = ticks;
        return this;
    }

    @Override
    public AbstractWanderBackToPoint setHardObjective(boolean hardObjective) {
        this.priority = -1;
        this.hardObjective = hardObjective;
        return this;
    }

    @Override
    public AbstractWanderBackToPoint setTeleportOnFail(boolean teleportOnFail) {
        this.teleportOnFail = teleportOnFail;
        return this;
    }

    @Override
    public AbstractWanderBackToPoint setStartWithCooldown(boolean startWithCooldown) {
        this.startWithCooldown = startWithCooldown;
        return this;
    }

    @Override
    public void updateCooldown() {
        this.lastTime = System.currentTimeMillis();
    }

    @Override
    public void register() {
        if (startWithCooldown) updateCooldown();
        mob.getBrain().addActivity(Activity.IDLE, priority, ImmutableList.of(this));
        if (hardObjective) mob.getBrain().addActivity(Activity.FIGHT, priority, ImmutableList.of(this));
    }

    @Override
    public void unregister() {
        //todo: ufortunately resetting brains is significantly harder than goals, this may be implemented later
    }
}
