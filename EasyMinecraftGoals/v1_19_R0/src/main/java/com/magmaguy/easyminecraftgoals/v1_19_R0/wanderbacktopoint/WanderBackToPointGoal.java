package com.magmaguy.easyminecraftgoals.v1_19_R0.wanderbacktopoint;

import com.magmaguy.easyminecraftgoals.events.WanderBackToPointEndEvent;
import com.magmaguy.easyminecraftgoals.events.WanderBackToPointStartEvent;
import com.magmaguy.easyminecraftgoals.internal.AbstractWanderBackToPoint;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;

import java.util.EnumSet;

public class WanderBackToPointGoal extends Goal implements AbstractWanderBackToPoint {

    private Vec3 returnLocation;
    private double maximumDistanceFromPoint;
    private LivingEntity livingEntity;
    private PathfinderMob pathfinderMob;
    private int priority;

    private Mob mob;
    private Path path = null;
    private long lastTime;

    private float speed;
    private int stopReturnDistance = 0;
    //Note: This assumes 20 tps
    private int goalRefreshCooldownTicks = 3 * 20;
    private int maxDurationTicks = 5 * 20;
    private boolean hardObjective = false;
    private boolean teleportOnFail = false;
    private boolean startWithCooldown = false;

    public WanderBackToPointGoal(Mob mob,
                                 LivingEntity livingEntity,
                                 PathfinderMob pathfinderMob,
                                 Location location,
                                 double maximumDistanceFromPoint,
                                 int priority,
                                 int maxDurationTicks) {
        this.mob = mob;
        this.livingEntity = livingEntity;
        this.pathfinderMob = pathfinderMob;
        this.returnLocation = new Vec3(location.getX(), location.getY(), location.getZ());
        this.priority = priority;
        this.maxDurationTicks = maxDurationTicks;

        this.speed = (float) livingEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue();
        this.maximumDistanceFromPoint = maximumDistanceFromPoint;
        lastTime = 0;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public void start() {
        this.pathfinderMob.getNavigation().moveTo(path, speed);
    }

    @Override
    public void tick() {
    }

    @Override
    public void stop() {
        if (pathfinderMob != null) {
            this.pathfinderMob.getNavigation().stop();
            this.path = null;
        }
        if (hardObjective && (pathfinderMob == null || pathfinderMob.isPathFinding()))
            livingEntity.teleport(new Location(livingEntity.getWorld(), returnLocation.x, returnLocation.y, returnLocation.z));
        WanderBackToPointEndEvent wanderBackToPointEndEvent = new WanderBackToPointEndEvent(hardObjective, livingEntity);
        Bukkit.getPluginManager().callEvent(wanderBackToPointEndEvent);
        updateCooldown();
    }

    @Override
    public boolean canUse() {
        if (!hardObjective && mob.getTarget() instanceof Player) {
            updateCooldown();
            return false;
        }
        if ((lastTime + 50L * goalRefreshCooldownTicks) - System.currentTimeMillis() > 0) return false;
        updateCooldown();
        if (pathfinderMob != null) {
            if (returnLocation.distanceToSqr(new Vec3(pathfinderMob.getX(), pathfinderMob.getY(), pathfinderMob.getZ())) < maximumDistanceFromPoint * maximumDistanceFromPoint)
                return false;
            path = this.pathfinderMob.getNavigation().createPath(returnLocation.x, returnLocation.y, returnLocation.z, stopReturnDistance);
            // Not 100% sure of why this is happening, but I suspect that sometimes the path can't be updated
            if (path == null) return false;
        }
        WanderBackToPointStartEvent wanderBackToPointStartEvent = new WanderBackToPointStartEvent(hardObjective, livingEntity);
        Bukkit.getPluginManager().callEvent(wanderBackToPointStartEvent);
        if (wanderBackToPointStartEvent.isCancelled()) return false;
        if (teleportOnFail && (pathfinderMob == null || !path.canReach())) {
            earlyPathfindingTermination();
            return false;
        }
        return true;
    }

    private void earlyPathfindingTermination() {
        livingEntity.teleport(new Location(livingEntity.getWorld(), returnLocation.x, returnLocation.y, returnLocation.z));
        WanderBackToPointEndEvent wanderBackToPointEndEvent = new WanderBackToPointEndEvent(hardObjective, livingEntity);
        Bukkit.getPluginManager().callEvent(wanderBackToPointEndEvent);
    }

    @Override
    public boolean canContinueToUse() {
        if ((lastTime + 50L * maxDurationTicks) - System.currentTimeMillis() < 0) return false;
        if (!hardObjective && mob.getTarget() instanceof Player) return false;
        return !pathfinderMob.getNavigation().isDone();
    }

    @Override
    public boolean isInterruptable() {
        return !hardObjective;
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
        mob.goalSelector.addGoal(priority, this);
    }

    @Override
    public void unregister() {
        mob.goalSelector.removeGoal(this);
    }
}
