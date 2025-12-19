package com.magmaguy.easyminecraftgoals.v1_21_R7_common;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

/**
 * Bridge class that handles CraftBukkit package path differences between Paper and Spigot.
 * Paper uses: org.bukkit.craftbukkit.*
 * Spigot uses: org.bukkit.craftbukkit.v1_21_R7.*
 *
 * All methods are cached after first invocation for performance.
 */
public class CraftBukkitBridge {

    private static final boolean IS_PAPER;
    private static final String CB_PACKAGE;

    // Cached classes
    private static Class<?> craftWorldClass;
    private static Class<?> craftPlayerClass;
    private static Class<?> craftEntityClass;
    private static Class<?> craftLivingEntityClass;
    private static Class<?> craftItemStackClass;
    private static Class<?> craftBlockDataClass;

    // Cached methods
    private static Method craftWorldGetHandle;
    private static Method craftPlayerGetHandle;
    private static Method craftEntityGetHandle;
    private static Method craftLivingEntityGetHandle;
    private static Method craftItemStackAsNMSCopy;
    private static Method craftBlockDataGetState;

    static {
        IS_PAPER = detectPaper();
        CB_PACKAGE = IS_PAPER ? "org.bukkit.craftbukkit" : "org.bukkit.craftbukkit.v1_21_R7";
        initializeClasses();
    }

    private static boolean detectPaper() {
        try {
            Class.forName("io.papermc.paper.configuration.GlobalConfiguration");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void initializeClasses() {
        try {
            craftWorldClass = Class.forName(CB_PACKAGE + ".CraftWorld");
            craftPlayerClass = Class.forName(CB_PACKAGE + ".entity.CraftPlayer");
            craftEntityClass = Class.forName(CB_PACKAGE + ".entity.CraftEntity");
            craftLivingEntityClass = Class.forName(CB_PACKAGE + ".entity.CraftLivingEntity");
            craftItemStackClass = Class.forName(CB_PACKAGE + ".inventory.CraftItemStack");
            craftBlockDataClass = Class.forName(CB_PACKAGE + ".block.data.CraftBlockData");

            // Cache methods
            craftWorldGetHandle = craftWorldClass.getMethod("getHandle");
            craftPlayerGetHandle = craftPlayerClass.getMethod("getHandle");
            craftEntityGetHandle = craftEntityClass.getMethod("getHandle");
            craftLivingEntityGetHandle = craftLivingEntityClass.getMethod("getHandle");
            craftItemStackAsNMSCopy = craftItemStackClass.getMethod("asNMSCopy", org.bukkit.inventory.ItemStack.class);
            craftBlockDataGetState = craftBlockDataClass.getMethod("getState");

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize CraftBukkit bridge for " + (IS_PAPER ? "Paper" : "Spigot"), e);
        }
    }

    public static boolean isPaper() {
        return IS_PAPER;
    }

    public static ServerLevel getServerLevel(World world) {
        try {
            return (ServerLevel) craftWorldGetHandle.invoke(world);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get ServerLevel from World", e);
        }
    }

    public static ServerLevel getServerLevel(Location location) {
        return getServerLevel(location.getWorld());
    }

    public static ServerPlayer getServerPlayer(Player player) {
        try {
            return (ServerPlayer) craftPlayerGetHandle.invoke(player);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get ServerPlayer from Player", e);
        }
    }

    public static Entity getNMSEntity(org.bukkit.entity.Entity entity) {
        try {
            return (Entity) craftEntityGetHandle.invoke(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get NMS Entity from Bukkit Entity", e);
        }
    }

    public static net.minecraft.world.entity.LivingEntity getNMSLivingEntity(LivingEntity entity) {
        try {
            return (net.minecraft.world.entity.LivingEntity) craftLivingEntityGetHandle.invoke(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get NMS LivingEntity from Bukkit LivingEntity", e);
        }
    }

    public static ItemStack asNMSCopy(org.bukkit.inventory.ItemStack itemStack) {
        try {
            return (ItemStack) craftItemStackAsNMSCopy.invoke(null, itemStack);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert ItemStack to NMS", e);
        }
    }

    public static BlockState getBlockState(BlockData blockData) {
        try {
            return (BlockState) craftBlockDataGetState.invoke(blockData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get BlockState from BlockData", e);
        }
    }

    /**
     * Gets the Bukkit World from an NMS Level.
     * Uses reflection to avoid the versioned CraftWorld return type issue.
     */
    public static World getBukkitWorld(net.minecraft.world.level.Level level) {
        try {
            // Call getWorld() via reflection to avoid compile-time binding to CraftWorld
            Method getWorld = level.getClass().getMethod("getWorld");
            return (World) getWorld.invoke(level);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Bukkit World from Level", e);
        }
    }

    /**
     * Gets the Bukkit Entity from an NMS Entity.
     * Uses reflection to avoid the versioned CraftEntity return type issue.
     */
    public static org.bukkit.entity.Entity getBukkitEntity(Entity nmsEntity) {
        try {
            // Call getBukkitEntity() via reflection to avoid compile-time binding to CraftEntity
            Method getBukkitEntity = nmsEntity.getClass().getMethod("getBukkitEntity");
            return (org.bukkit.entity.Entity) getBukkitEntity.invoke(nmsEntity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Bukkit Entity from NMS Entity", e);
        }
    }

    // Cached method for Display teleport duration
    private static Method displayTeleportDurationMethod = null;

    /**
     * Sets the teleport/position interpolation duration on a Display entity.
     * Uses reflection with fallback since the method is private and string literals aren't remapped.
     * Mojang mapping: setPosRotInterpolationDuration
     */
    public static void setDisplayTeleportDuration(net.minecraft.world.entity.Display display, int duration) {
        try {
            if (displayTeleportDurationMethod == null) {
                displayTeleportDurationMethod = findDisplayTeleportDurationMethod();
            }
            displayTeleportDurationMethod.invoke(display, duration);
        } catch (Exception e) {
            // Non-critical - just log and continue
            e.printStackTrace();
        }
    }

    private static Method findDisplayTeleportDurationMethod() throws NoSuchMethodException {
        Class<?> displayClass = net.minecraft.world.entity.Display.class;

        // Paper uses Mojang mappings, Spigot uses obfuscated
        String methodName = IS_PAPER ? "setPosRotInterpolationDuration" : "d";

        Method method = displayClass.getDeclaredMethod(methodName, int.class);
        method.setAccessible(true);
        return method;
    }

    /**
     * Gets the field name for entity dimensions.
     * Paper uses Mojang mappings ("dimensions"), Spigot uses obfuscated ("bz").
     * Note: After specialsource remapping, Spigot code will use the correct obfuscated name.
     */
    public static String getEntityDimensionsFieldName() {
        // This will be "dimensions" in source, but specialsource will remap it for Spigot
        return "dimensions";
    }
}
