package com.magmaguy.easyminecraftgoals;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public class NMSManager {
    private static final String PACKAGE = "com.magmaguy.easyminecraftgoals.";

    private static NMSAdapter adapter;

    public static Plugin pluginProvider;

    public static void initializeAdapter(Plugin plugin) {
        String version = getServerVersion();
        pluginProvider = plugin;

        plugin.getLogger().info("Sanity check!");
        try {
            plugin.getLogger().info("Format: " + PACKAGE + version + ".NMSAdapter");
            adapter = (NMSAdapter) Class.forName(PACKAGE + version + ".NMSAdapter").getDeclaredConstructor().newInstance();
            plugin.getLogger().log(Level.INFO, "Supported server version detected: {0}", version);
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().log(Level.SEVERE, "Server version \"{0}\" is unsupported! Please check for updates!",
                    version);
            //todo: remains to be seen Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    public static NMSAdapter getAdapter() {
        return adapter;
    }

    private static String getServerVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }
}
