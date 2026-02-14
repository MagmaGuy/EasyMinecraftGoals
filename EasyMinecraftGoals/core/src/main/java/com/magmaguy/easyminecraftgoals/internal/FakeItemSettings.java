package com.magmaguy.easyminecraftgoals.internal;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Display;
import org.bukkit.inventory.ItemStack;

/**
 * Settings container for FakeItem instances.
 * Stores all configuration options before the FakeItem is created.
 */
@Getter
@Setter
public class FakeItemSettings {
    private ItemStack itemStack;
    private Display.Billboard billboard = Display.Billboard.FIXED;
    private float scale = 1.0f;
    private float viewRange = 1.0f;
    private boolean glowing = false;
    private String customName = null;
    private boolean customNameVisible = false;

    public FakeItemSettings() {
    }

    /**
     * Copy constructor for creating a snapshot of settings.
     */
    public FakeItemSettings(FakeItemSettings other) {
        this.itemStack = other.itemStack != null ? other.itemStack.clone() : null;
        this.billboard = other.billboard;
        this.scale = other.scale;
        this.viewRange = other.viewRange;
        this.glowing = other.glowing;
        this.customName = other.customName;
        this.customNameVisible = other.customNameVisible;
    }

    public boolean hasCustomName() {
        return customName != null && !customName.isEmpty();
    }
}
