package com.magmaguy.easyminecraftgoals.thirdparty;

import org.bukkit.entity.Player;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.connection.GeyserConnection;

/**
 * Geyser integration for Bedrock player detection.
 * This class is only loaded when Geyser-Spigot is present.
 */
class Geyser {
    private Geyser() {
    }

    static boolean isBedrock(Player player) {
        GeyserConnection geyserConnection = GeyserApi.api().connectionByUuid(player.getUniqueId());
        return geyserConnection != null;
    }
}
