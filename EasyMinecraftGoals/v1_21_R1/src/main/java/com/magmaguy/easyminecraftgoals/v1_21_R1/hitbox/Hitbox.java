package com.magmaguy.easyminecraftgoals.v1_21_R1.hitbox;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.AABB;

import java.lang.reflect.Field;

public class Hitbox {
    private Hitbox() {
    }

    public static boolean setCustomHitbox(Entity entity, float width, float height, boolean fixed) {
        EntityDimensions entityDimensions = new EntityDimensions(width, height, height, null, fixed);
        AABB boundingBox = entityDimensions.makeBoundingBox(entity.position());

        Class<?> entityClass = Entity.class;
        try {
            Field field = entityClass.getDeclaredField("bd");
            field.setAccessible(true);
            field.set(entity, boundingBox);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
        entity.setBoundingBox(boundingBox);
        return true;
    }
}
