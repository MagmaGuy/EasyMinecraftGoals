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
        pluginProvider = plugin;
//        plugin.getLogger().info(Bukkit.getServer().getClass().getPackage().getName());
        String version = getServerVersion();
        if (version == null) {
            plugin.getLogger().warning("Server version is null.");
            return;
        }

        try {
//            plugin.getLogger().info("Format: " + PACKAGE + version + ".NMSAdapter");
            String versionName;
            //1.20.0 is fundamentally the same as 1.20.1 so we use R2
            if (Objects.equals(version, "v1_20_R0")) versionName = PACKAGE + "v1_20_R1" + ".NMSAdapter";
            else versionName = PACKAGE + version + ".NMSAdapter";
//            plugin.getLogger().info("Loading class: " + versionName);
            adapter = (NMSAdapter) Class.forName(versionName).getDeclaredConstructor().newInstance();
            plugin.getLogger().log(Level.INFO, "Supported server version detected: {0}", version);
            isEnabled = true;
        } catch (ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "Class not found: {0}", e.getMessage());
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().log(Level.SEVERE, "Error instantiating class: {0}", e.getMessage());
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Unexpected error: {0}", e.getMessage());
        } finally {
            if (!isEnabled) {
                plugin.getLogger().log(Level.SEVERE, "Server version \"{0}\" is unsupported! Please check for updates!", version);
                //todo: remains to be seen Bukkit.getPluginManager().disablePlugin(plugin);
            }
        }
    }

    public static NMSAdapter getAdapter() {
        return adapter;
    }

    private static String getServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        if (packageName.contains("_R")) {
            return packageName.split("\\.")[3];
        }

        String versionString = Bukkit.getServer().getVersion();
        if (!versionString.contains("-")) {
            pluginProvider.getLogger().warning("Incompatible Minecraft version detected! [1] Package: " + packageName + " version: " + versionString + " ! Report this to the developer.");
            return null;
        }

        try {
            String justVersion = versionString.split("-")[0];
            int major = Integer.parseInt(justVersion.split("\\.")[1]);
            int minor;
            try {
                minor = Integer.parseInt(justVersion.split("\\.")[2]);
            } catch (Exception e) {
                minor = 0;
            }
            return getInternalsFromRevision(major, minor);
        } catch (Exception e) {
            pluginProvider.getLogger().warning("Incompatible Minecraft version detected! [2] Package: " + packageName + " version: " + versionString + " ! Report this to the developer.");
            return null;
        }
    }

    private static String getInternalsFromRevision(int major, int minor) {
        String versionString = "v1_";
        if (major == 20) {
            versionString += "20_";
            if (minor == 6)
                return versionString + "R4";
        } else if (major == 21) {
            versionString += "21_";
            if (minor == 0 || minor == 1)
                return versionString + "R1";
            if (minor == 2 || minor == 3)
                return versionString + "R2";
            if (minor == 4)
                return versionString + "R3";
            if (minor == 5)
                return versionString + "R4";
            if (minor == 6 || minor == 7 )
                return versionString + "R5";
//            if (minor == 7 || minor == 8)
//                return versionString + "R6";
        }
        pluginProvider.getLogger().warning(
                "Incompatible Minecraft version detected! [3] Package: " +
                        Bukkit.getServer().getClass().getPackage().getName() +
                        " version: " + Bukkit.getServer().getVersion() + " and attempted to get " + versionString
                        + " ! Report this to the developer.");
        return null;
    }
}
