package com.magmaguy.easyminecraftgoals.internal;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface PacketEntityInterface {
    void addRemoveCallback(Runnable callback);

    void displayTo(UUID uuid);

    void hideFrom(UUID uuid);

    void remove();

    void setVisible(boolean visible);

    Location getLocation();

    UUID getUniqueId();

    void teleport(Location location);

    void addViewer(UUID uuid);

    void removeViewer(UUID uuid);

}
