package com.magmaguy.easyminecraftgoals.v1_21_R7_common;

import com.magmaguy.easyminecraftgoals.v1_21_R7_common.massblockedit.MassEditBlocks;
import com.magmaguy.easyminecraftgoals.v1_21_R7_common.move.Move;
import com.magmaguy.easyminecraftgoals.v1_21_R7_common.entitydata.BodyRotation;
import com.magmaguy.easyminecraftgoals.v1_21_R7_common.hitbox.Hitbox;
import com.magmaguy.easyminecraftgoals.v1_21_R7_common.packets.PacketArmorStandEntity;
import com.magmaguy.easyminecraftgoals.v1_21_R7_common.packets.PacketBundle;
import com.magmaguy.easyminecraftgoals.v1_21_R7_common.packets.PacketDisplayEntity;
import com.magmaguy.easyminecraftgoals.v1_21_R7_common.wanderbacktopoint.WanderBackToPointBehavior;
import com.magmaguy.easyminecraftgoals.v1_21_R7_common.wanderbacktopoint.WanderBackToPointGoal;
import com.magmaguy.easyminecraftgoals.internal.AbstractPacketBundle;
import com.magmaguy.easyminecraftgoals.internal.AbstractWanderBackToPoint;
import com.magmaguy.easyminecraftgoals.internal.PacketModelEntity;
import com.magmaguy.easyminecraftgoals.internal.PacketTextEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.LivingEntity;

public class NMSAdapter implements com.magmaguy.easyminecraftgoals.NMSAdapter {

    @Override
    public boolean setCustomHitbox(org.bukkit.entity.Entity entity, float width, float height, boolean fixed) {
        Entity nmsEntity = CraftBukkitBridge.getNMSEntity(entity);
        return Hitbox.setCustomHitbox(nmsEntity, width, height, fixed);
    }

    @Override
    public float getBodyRotation(org.bukkit.entity.Entity entity) {
        Entity nmsEntity = CraftBukkitBridge.getNMSEntity(entity);
        return BodyRotation.getBodyRotation(nmsEntity);
    }

    @Override
    public boolean pathfinderMoveToLocation(LivingEntity livingEntity, double speedModifier, Location destination) {
        PathfinderMob pathfinderMob = (PathfinderMob) CraftBukkitBridge.getNMSLivingEntity(livingEntity);
        return Move.simpleMove(pathfinderMob, speedModifier, destination);
    }

    @Override
    public void pathfinderStopMoving(LivingEntity livingEntity) {
        PathfinderMob pathfinderMob = (PathfinderMob) CraftBukkitBridge.getNMSLivingEntity(livingEntity);
        Move.doNotMove(pathfinderMob);
    }

    @Override
    public boolean pathfinderCanReach(LivingEntity livingEntity, Location destination) {
        PathfinderMob pathfinderMob = (PathfinderMob) CraftBukkitBridge.getNMSLivingEntity(livingEntity);
        return Move.canReach(pathfinderMob, destination);
    }

    @Override
    public void universalMoveToLocation(LivingEntity livingEntity, double speedModifier, Location destination) {
        Mob mob = (Mob) CraftBukkitBridge.getNMSLivingEntity(livingEntity);
        Move.universalMove(mob, speedModifier, destination);
    }

    @Override
    public boolean forcedMoveToLocation(LivingEntity livingEntity, double speedModifier, Location destination) {
        Mob mob = (Mob) CraftBukkitBridge.getNMSLivingEntity(livingEntity);
        return Move.forcedMove(mob, speedModifier, destination);
    }

    @Override
    public AbstractWanderBackToPoint getWanderBackToPoint(LivingEntity livingEntity, Location location, double maximumDistanceFromPoint, int priority, int maxDurationTicks, boolean useBrain) {
        net.minecraft.world.entity.LivingEntity nmsLivingEntity = CraftBukkitBridge.getNMSLivingEntity(livingEntity);
        if (useBrain)
            return new WanderBackToPointBehavior(livingEntity, (Mob) nmsLivingEntity, location, maximumDistanceFromPoint, priority, maxDurationTicks);
        else {
            Mob mob = (Mob) nmsLivingEntity;
            PathfinderMob pathfinderMob = mob instanceof PathfinderMob ? (PathfinderMob) mob : null;
            return new WanderBackToPointGoal(mob, livingEntity, pathfinderMob, location, maximumDistanceFromPoint, priority, maxDurationTicks);
        }
    }

    @Override
    public void setBlockInNativeDataPalette(World world, int x, int y, int z, BlockData blockData, boolean applyPhysics) {
        MassEditBlocks.setBlockInNativeDataPalette(world, x, y, z, blockData, applyPhysics);
    }

    @Override
    public PacketModelEntity generatePacketArmorStandEntity(Location location, String modelID) {
        PacketArmorStandEntity packetArmorStandEntity = new PacketArmorStandEntity(location);
        packetArmorStandEntity.initializeModel(location, modelID);
        return packetArmorStandEntity;
    }

    @Override
    public PacketModelEntity generatePacketDisplayEntity(Location location, String modelID) {
        PacketDisplayEntity packetDisplayEntity = new PacketDisplayEntity(location);
        packetDisplayEntity.initializeModel(location, modelID);
        return packetDisplayEntity;
    }

    @Override
    public PacketTextEntity generatePacketTextEntity(Location location) {
        PacketArmorStandEntity packetArmorStandEntity = new PacketArmorStandEntity(location);
        packetArmorStandEntity.initializeText(location);
        return packetArmorStandEntity;
    }

    @Override
    public AbstractPacketBundle createPacketBundle() {
        return new PacketBundle();
    }
}
