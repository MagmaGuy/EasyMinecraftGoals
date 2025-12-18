package com.magmaguy.easyminecraftgoals.v1_21_R7_paper.hitbox;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;

import java.lang.reflect.Field;

public class Hitbox {
    private Hitbox() {
    }

    public static boolean setCustomHitbox(Entity entity, float width, float height, boolean fixed) {
        EntityDimensions entityDimensions = new EntityDimensions(width, height, height, null, fixed);
        Class<?> entityClass = Entity.class;
        try {
            // Paper uses Mojang mappings - field name is "dimensions"
            Field field = entityClass.getDeclaredField("dimensions");
            field.setAccessible(true);
            field.set(entity, entityDimensions);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
        entity.setBoundingBox(entityDimensions.makeBoundingBox(entity.position()));
        return true;
    }
}
