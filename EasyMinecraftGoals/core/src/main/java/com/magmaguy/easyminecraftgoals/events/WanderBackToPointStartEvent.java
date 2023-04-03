package com.magmaguy.easyminecraftgoals.events;

import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WanderBackToPointStartEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final boolean hardObjective;
    @Getter
    private final LivingEntity livingEntity;
    private boolean cancelled = false;

    public WanderBackToPointStartEvent(boolean hard, LivingEntity livingEntity) {
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

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
