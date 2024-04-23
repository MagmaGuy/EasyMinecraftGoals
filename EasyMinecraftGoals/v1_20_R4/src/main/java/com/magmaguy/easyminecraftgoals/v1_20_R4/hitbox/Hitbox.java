package com.magmaguy.easyminecraftgoals.v1_20_R4.hitbox;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class Hitbox {
    private Hitbox() {
    }

    public static boolean setCustomHitbox(Entity entity, float width, float height, boolean fixed) {
        try {
            Constructor<EntityDimensions> constructor = EntityDimensions.class.getDeclaredConstructor(float.class, float.class, boolean.class);
            constructor.setAccessible(true);
            EntityDimensions entityDimensions = constructor.newInstance(width, height, fixed);
            Class<?> entityClass = Entity.class;
            try {
                Field field = entityClass.getDeclaredField("aI");
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
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
                 InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }
}
