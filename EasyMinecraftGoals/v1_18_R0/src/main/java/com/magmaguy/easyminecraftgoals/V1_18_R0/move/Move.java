package com.magmaguy.easyminecraftgoals.V1_18_R0.move;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.pathfinder.Path;
import org.bukkit.Location;

public class Move {
    public static boolean simpleMove(PathfinderMob pathfinderMob, double speedModifier, Location destination) {
        Path path = pathfinderMob.getNavigation().createPath(destination.getX(), destination.getY(), destination.getZ(), 0);
        return pathfinderMob.getNavigation().moveTo(path, speedModifier);
    }
}
