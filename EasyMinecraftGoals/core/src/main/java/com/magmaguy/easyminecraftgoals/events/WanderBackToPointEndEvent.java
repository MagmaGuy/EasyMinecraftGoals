package com.magmaguy.easyminecraftgoals.events;

import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WanderBackToPointEndEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final boolean hardObjective;
    @Getter
    private final LivingEntity livingEntity;

    public WanderBackToPointEndEvent(boolean hard, LivingEntity livingEntity) {
        this.hardObjective = hard;
        this.livingEntity = livingEntity;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
