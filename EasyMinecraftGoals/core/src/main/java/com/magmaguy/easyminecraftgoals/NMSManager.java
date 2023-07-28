package com.magmaguy.easyminecraftgoals;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.logging.Level;

public class NMSManager {
    private static final String PACKAGE = "com.magmaguy.easyminecraftgoals.";
    public static Plugin pluginProvider;
    private static NMSAdapter adapter;
    @Getter
    private static boolean isEnabled = false;

    public static void initializeAdapter(Plugin plugin) {
        String version = getServerVersion();
        pluginProvider = plugin;

        try {
            plugin.getLogger().info("Format: " + PACKAGE + version + ".NMSAdapter");
            String versionName;
            //1.20.0 is fundamentally the same as 1.20.1 so we use R2
            if (Objects.equals(version, "v1_20_R1")) versionName = PACKAGE + "v1_20_R2" + ".NMSAdapter";
            else versionName = PACKAGE + version + ".NMSAdapter";
            adapter = (NMSAdapter) Class.forName(versionName).getDeclaredConstructor().newInstance();
            plugin.getLogger().log(Level.INFO, "Supported server version detected: {0}", version);
            isEnabled = true;
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().log(Level.SEVERE, "Server version \"{0}\" is unsupported! Please check for updates!",
                    version);
            //todo: remains to be seen Bukkit.getPluginManager().disablePlugin(plugin);
            isEnabled = false;
        }
    }

    public static NMSAdapter getAdapter() {
        return adapter;
    }

    private static String getServerVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }
}
