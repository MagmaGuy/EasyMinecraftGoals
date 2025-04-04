package com.magmaguy.easyminecraftgoals.v1_20_R1.Move;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Move {
    public static boolean canReach(PathfinderMob pathfinderMob, Location destination) {
        if (pathfinderMob.getNavigation() == null) return true;
        Path path = pathfinderMob.getNavigation().createPath(destination.getX(), destination.getY(), destination.getZ(), 0);
        if (path == null) return true;
        return path.canReach();
    }

    public static boolean simpleMove(PathfinderMob pathfinderMob, double speedModifier, Location destination) {
        Path path = pathfinderMob.getNavigation().createPath(destination.getX(), destination.getY(), destination.getZ(), 0);
        return pathfinderMob.getNavigation().moveTo(path, speedModifier);
    }

    public static void doNotMove(PathfinderMob pathfinderMob) {
        pathfinderMob.getNavigation().moveTo(pathfinderMob,0);
    }

    public static void universalMove(Mob mob, double speedModifier, Location destination) {
        double speed = mob.getAttributeValue(Attributes.MOVEMENT_SPEED) * .75;
        Vec3 movementInTick = new Vec3(destination.getX(), destination.getY(), destination.getZ()).subtract(mob.position());
        movementInTick = movementInTick.normalize().multiply(speedModifier * speed, speedModifier * speed, speedModifier * speed);
        mob.move(MoverType.SELF, movementInTick);
        rotateHead(mob, destination.toVector(), new Vector(mob.position().x, mob.position().y, mob.position().z));
        //setDirection(destination.toVector().subtract(new Vector(mob.position().x, mob.position().y, mob.position().z)),pathfinderMob);
    }


    private static void rotateHead(Entity entity, Vector destination, Vector currentLocation) {
        Vector newVector = destination.subtract(currentLocation);
        double x = newVector.getX();
        double z = newVector.getZ();
        double targetRot;
        if (Math.abs(x) > Math.abs(z)) {
            if (x > 0)
                targetRot = -90;
            else
                targetRot = 90;
        } else {
            if (z > 0)
                targetRot = 0;
            else
                targetRot = 180;
        }
        double currentRot = entity.getYRot();
        if (currentRot == targetRot) return;
        if (targetRot - currentRot > 0)
            entity.turn(90D, 0D);
        else
            entity.turn(-90D, 0D);
    }

    /*
    public static void setDirection(Vector direction, Entity entity) {
        final double _2PI = 2 * Math.PI;
        final double x = direction.getX();
        final double z = direction.getZ();

        if (x == 0 && z == 0) {
            return;
        }

        double theta = Math.atan2(-x, z);
        float yaw = (float) Math.toDegrees((theta + _2PI) % _2PI);

        entity.setYRot(yaw);
        entity.yRotO = yaw;
    }
     */

    public static boolean forcedMove(Mob mob, double speedModifier, Location destination) {
        //Lobotomize the mob
        mob.removeFreeWill();

        universalMove(mob, speedModifier, destination);
        return true;
    }
}
