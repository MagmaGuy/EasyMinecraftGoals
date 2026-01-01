package com.magmaguy.easyminecraftgoals.thirdparty;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Utility class to detect if a player is connecting from Bedrock Edition.
 * Supports Floodgate and Geyser-Spigot plugins.
 */
public class BedrockChecker {
    private BedrockChecker() {
    }

    /**
     * Checks if a player is connecting from Bedrock Edition.
     * Requires either Floodgate or Geyser-Spigot plugin to be installed.
     *
     * @param player The player to check
     * @return true if the player is on Bedrock Edition, false otherwise
     */
    public static boolean isBedrock(Player player) {
        if (Bukkit.getPluginManager().isPluginEnabled("floodgate"))
            return Floodgate.isBedrock(player);
        else if (Bukkit.getPluginManager().isPluginEnabled("Geyser-Spigot"))
            return Geyser.isBedrock(player);
        else return false;
    }
}
