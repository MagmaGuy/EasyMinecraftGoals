package com.magmaguy.easyminecraftgoals.events;

import com.magmaguy.easyminecraftgoals.internal.AbstractWanderBackToPoint;
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
    @Getter
    private AbstractWanderBackToPoint abstractWanderBackToPoint;

    public WanderBackToPointEndEvent(boolean hard, LivingEntity livingEntity, AbstractWanderBackToPoint abstractWanderBackToPoint) {
        this.hardObjective = hard;
        this.livingEntity = livingEntity;
        this.abstractWanderBackToPoint = abstractWanderBackToPoint;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
