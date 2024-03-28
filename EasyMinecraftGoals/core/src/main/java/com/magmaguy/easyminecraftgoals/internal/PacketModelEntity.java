package com.magmaguy.easyminecraftgoals.internal;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.util.EulerAngle;

public interface PacketModelEntity extends PacketEntityInterface {
    void sendLocationAndRotationPacket(Location location, EulerAngle eulerAngle);

    void initializeModel(Location location, int modelID);

    void setScale(float scale);

    void setHorseLeatherArmorColor(Color color);
}
