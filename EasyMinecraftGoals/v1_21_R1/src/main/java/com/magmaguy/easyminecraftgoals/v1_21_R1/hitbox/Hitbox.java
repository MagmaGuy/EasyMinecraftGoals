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

        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        // Calculate the half dimensions
        double halfWidth = entityDimensions.width() / 2.0;
        double dHeight = entityDimensions.height();

        AABB boundingBox = new AABB(
                x - halfWidth, y, z - halfWidth,  // min corner
                x + halfWidth, y + dHeight, z + halfWidth  // max corner
        );

        Class<?> entityClass = Entity.class;
        try {
            Field field = entityClass.getDeclaredField("aF");
            field.setAccessible(true);
            field.set(entity, boundingBox);
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
