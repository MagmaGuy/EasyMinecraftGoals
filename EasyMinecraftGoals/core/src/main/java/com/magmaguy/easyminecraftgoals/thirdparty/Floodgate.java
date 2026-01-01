package com.magmaguy.easyminecraftgoals.thirdparty;

import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

/**
 * Floodgate integration for Bedrock player detection.
 * This class is only loaded when Floodgate is present.
 */
class Floodgate {
    private Floodgate() {
    }

    static boolean isBedrock(Player player) {
        return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
    }
}
