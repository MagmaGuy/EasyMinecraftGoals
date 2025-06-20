package com.magmaguy.easyminecraftgoals.internal;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.util.EulerAngle;

public interface PacketModelEntity extends PacketEntityInterface {
    void sendLocationAndRotationPacket(Location location, EulerAngle eulerAngle);

    void sendLocationAndRotationAndScalePacket(Location location, EulerAngle eulerAngle, float scale);

    default void initializeModel(Location location, int modelID) {
        throw new UnsupportedOperationException("Integer modelID not supported by this implementation.");
    }

    default void initializeModel(Location location, String modelID) {
        throw new UnsupportedOperationException("String modelID not supported by this implementation.");
    }

    void setScale(float scale);

    void setHorseLeatherArmorColor(Color color);

    boolean hasViewers();
}
