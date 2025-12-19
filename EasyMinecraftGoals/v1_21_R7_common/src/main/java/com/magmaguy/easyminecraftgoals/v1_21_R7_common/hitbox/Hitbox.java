package com.magmaguy.easyminecraftgoals.v1_21_R7_common.hitbox;

import com.magmaguy.easyminecraftgoals.v1_21_R7_common.CraftBukkitBridge;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;

import java.lang.reflect.Field;

public class Hitbox {
    // Cache the field for performance
    private static Field dimensionsField = null;

    private Hitbox() {
    }

    public static boolean setCustomHitbox(Entity entity, float width, float height, boolean fixed) {
        EntityDimensions entityDimensions = new EntityDimensions(width, height, height, null, fixed);
        try {
            if (dimensionsField == null) {
                dimensionsField = findDimensionsField();
            }
            dimensionsField.set(entity, entityDimensions);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        entity.setBoundingBox(entityDimensions.makeBoundingBox(entity.position()));
        return true;
    }

    private static Field findDimensionsField() throws NoSuchFieldException {
        // Paper uses Mojang mappings, Spigot uses obfuscated
        String fieldName = CraftBukkitBridge.isPaper() ? "dimensions" : "bz";

        Field field = Entity.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }
}
