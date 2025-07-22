package com.magmaguy.easyminecraftgoals.internal;

import org.bukkit.entity.Player;

import java.util.List;

public interface AbstractPacketBundle {
     void addPacket(Object packet, List<Player> viewers) ;
     void send() ;
}
