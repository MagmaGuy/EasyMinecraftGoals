package com.magmaguy.easyminecraftgoals.internal;

import org.bukkit.Location;

public interface PacketTextEntity extends PacketModelEntity{
    void setText(String text);
    void setTextVisible(boolean visible);
    void initializeText(Location location);
}
