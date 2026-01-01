package com.magmaguy.easyminecraftgoals.v1_21_R7_common;

import com.magmaguy.easyminecraftgoals.constants.OverridableWanderPriority;
import com.magmaguy.easyminecraftgoals.internal.AbstractPacketBundle;
import com.magmaguy.easyminecraftgoals.internal.AbstractWanderBackToPoint;
import com.magmaguy.easyminecraftgoals.internal.FakeText;
import com.magmaguy.easyminecraftgoals.internal.FakeTextSettings;
import com.magmaguy.easyminecraftgoals.internal.PacketEntityInterface;
import com.magmaguy.easyminecraftgoals.internal.PacketModelEntity;
import com.magmaguy.easyminecraftgoals.internal.PacketTextEntity;
import com.magmaguy.easyminecraftgoals.v1_21_R7_common.entitydata.BodyRotation;
import com.magmaguy.easyminecraftgoals.v1_21_R7_common.hitbox.Hitbox;
import com.magmaguy.easyminecraftgoals.v1_21_R7_common.massblockedit.MassEditBlocks;
import com.magmaguy.easyminecraftgoals.v1_21_R7_common.move.Move;
import com.magmaguy.easyminecraftgoals.v1_21_R7_common.packets.FakeTextImpl;
import com.magmaguy.easyminecraftgoals.v1_21_R7_common.packets.PacketArmorStandEntity;
import com.magmaguy.easyminecraftgoals.v1_21_R7_common.packets.PacketBundle;
import com.magmaguy.easyminecraftgoals.v1_21_R7_common.packets.PacketDisplayEntity;
import com.magmaguy.easyminecraftgoals.v1_21_R7_common.packets.PacketGenericEntity;
import com.magmaguy.easyminecraftgoals.v1_21_R7_common.wanderbacktopoint.WanderBackToPointBehavior;
import com.magmaguy.easyminecraftgoals.v1_21_R7_common.wanderbacktopoint.WanderBackToPointGoal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class NMSAdapter extends com.magmaguy.easyminecraftgoals.NMSAdapter {

    private PathfinderMob getPathfinderMob(Entity entity) {
        net.minecraft.world.entity.Entity nmsEntity = CraftBukkitBridge.getNMSEntity(entity);
        if (nmsEntity instanceof PathfinderMob pathfinderMob)
            return pathfinderMob;
        else
            return null;
    }

    @Override
    public PacketModelEntity createPacketArmorStandEntity(Location location) {
        return new PacketArmorStandEntity(location);
    }

    @Override
    public PacketModelEntity createPacketDisplayEntity(Location location) {
        return new PacketDisplayEntity(location);
    }

    @Override
    public PacketTextEntity createPacketTextArmorStandEntity(Location location) {
        return new PacketArmorStandEntity(location);
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
        net.minecraft.world.entity.Entity nmsEntity = CraftBukkitBridge.getNMSEntity(entity);
        return Hitbox.setCustomHitbox(nmsEntity, width, height, fixed);
    }

    @Override
    public float getBodyRotation(Entity entity) {
        net.minecraft.world.entity.Entity nmsEntity = CraftBukkitBridge.getNMSEntity(entity);
        return BodyRotation.getBodyRotation(nmsEntity);
    }

    @Override
    public boolean move(LivingEntity livingEntity, double speedModifier, Location location) {
        net.minecraft.world.entity.LivingEntity nmsLivingEntity = CraftBukkitBridge.getNMSLivingEntity(livingEntity);
        PathfinderMob pathfinderMob;
        if (nmsLivingEntity instanceof PathfinderMob pm)
            pathfinderMob = pm;
        else
            pathfinderMob = null;
        if (!(nmsLivingEntity instanceof Mob)) {
            Bukkit.getLogger().info("[EasyMinecraftPathfinding] Entity type " + livingEntity.getType() + " does not extend Mob and is therefore unable to have goals! It will not be able to pathfind.");
            return false;
        }
        return Move.simpleMove(pathfinderMob, speedModifier, location);
    }

    @Override
    public void doNotMove(LivingEntity livingEntity) {
        PathfinderMob pathfinderMob = getPathfinderMob(livingEntity);
        if (pathfinderMob == null) return;
        Move.doNotMove(pathfinderMob);
    }

    @Override
    public boolean forcedMove(LivingEntity livingEntity, double speedModifier, Location location) {
        net.minecraft.world.entity.LivingEntity nmsLivingEntity = CraftBukkitBridge.getNMSLivingEntity(livingEntity);
        if (!(nmsLivingEntity instanceof Mob mob)) {
            Bukkit.getLogger().info("[EasyMinecraftPathfinding] Entity type " + livingEntity.getType() + " does not extend Mob and is therefore unable to have goals! It will not be able to pathfind.");
            return false;
        }
        return Move.forcedMove(mob, speedModifier, location);
    }

    @Override
    public void universalMove(LivingEntity livingEntity, double speedModifier, Location location) {
        net.minecraft.world.entity.LivingEntity nmsLivingEntity = CraftBukkitBridge.getNMSLivingEntity(livingEntity);
        if (!(nmsLivingEntity instanceof Mob mob)) {
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
        net.minecraft.world.entity.LivingEntity nmsLivingEntity = CraftBukkitBridge.getNMSLivingEntity(livingEntity);
        PathfinderMob pathfinderMob;
        if (nmsLivingEntity instanceof PathfinderMob pm)
            pathfinderMob = pm;
        else
            pathfinderMob = null;
        if (!(nmsLivingEntity instanceof Mob mob)) {
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

    @Override
    public void setBlockInNativeDataPalette(World world, int x, int y, int z, BlockData blockData, boolean applyPhysics) {
        MassEditBlocks.setBlockInNativeDataPalette(world, x, y, z, blockData, applyPhysics);
    }

    @Override
    public AbstractPacketBundle createPacketBundle() {
        return new PacketBundle();
    }

    @Override
    public PacketEntityInterface createPacketEntity(EntityType entityType, Location location) {
        return new PacketGenericEntity(entityType, location);
    }

    @Override
    public FakeText createFakeText(Location location, FakeTextSettings settings) {
        return new FakeTextImpl(location, settings);
    }
}
