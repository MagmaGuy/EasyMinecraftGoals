package com.magmaguy.easyminecraftgoals.utils;

import org.bukkit.util.Vector;

public class Utils {
    private Utils() {
    }

    public static boolean distanceShorterThan(Vector source, Vector destination, double distance) {
        return source.distanceSquared(destination) < distance * distance;
    }

    public static boolean distanceGreaterThan(Vector source, Vector destination, double distance) {
        return source.distanceSquared(destination) > distance * distance;
    }
}
