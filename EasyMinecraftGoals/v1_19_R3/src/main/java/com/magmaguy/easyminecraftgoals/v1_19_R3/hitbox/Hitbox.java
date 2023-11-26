package com.magmaguy.easyminecraftgoals.v1_19_R3.hitbox;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;

public class Hitbox {
    private Hitbox() {
    }

    public static boolean setCustomHitbox(Entity entity, float width, float height, boolean fixed) {
        EntityDimensions entityDimensions = new EntityDimensions(width, height, fixed);
        Class<?> entityClass = Entity.class;
        try {
            Field field = entityClass.getDeclaredField("be");
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
