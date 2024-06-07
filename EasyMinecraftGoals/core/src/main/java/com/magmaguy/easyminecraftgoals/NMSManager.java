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
        plugin.getLogger().info(Bukkit.getServer().getClass().getPackage().getName());
        String version = getServerVersion();
        if (version == null) {return;}

        try {
            plugin.getLogger().info("Format: " + PACKAGE + version + ".NMSAdapter");
            String versionName;
            //1.20.0 is fundamentally the same as 1.20.1 so we use R2
            if (Objects.equals(version, "v1_20_R0")) versionName = PACKAGE + "v1_20_R1" + ".NMSAdapter";
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
        if (Bukkit.getServer().getClass().getPackage().getName().contains("_R"))
            return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        if (!Bukkit.getServer().getVersion().contains("-")) {
            pluginProvider.getLogger().warning("Incompatible Minecraft version detected! [1] Package: " + Bukkit.getServer().getClass().getPackage().getName() + " version: " + Bukkit.getServer().getVersion() + " ! Report this to the developer.");
            return null;
        }

        try {//Format: X.XX.XX-YYY-ZZZZZZZ where X is the actual version
            String justVersion = Bukkit.getServer().getVersion().split("-")[0];
            int major = Integer.parseInt(justVersion.split("\\.")[1]);
            int minor = Integer.parseInt(justVersion.split("\\.")[2]);
            return getInternalsFromRevision(major, minor);
        } catch (Exception e){
            pluginProvider.getLogger().warning("Incompatible Minecraft version detected! [2] Package: " + Bukkit.getServer().getClass().getPackage().getName() + " version: " + Bukkit.getServer().getVersion() + " ! Report this to the developer.");
            return null;
        }
        //paper doing paper things
    }

    //Paper decided to change internal versioning so this needs to get updated from 1.20.6 onward with mapping the version to the correct module
    private static String getInternalsFromRevision(int major, int minor){
        String versionString = "v1_";
        if (major == 20){
            versionString += "20_";
            if (minor == 6)
                return versionString + "R4";
        }
        pluginProvider.getLogger().warning("Incompatible Minecraft version detected! [3] Package: " + Bukkit.getServer().getClass().getPackage().getName() + " version: " + Bukkit.getServer().getVersion() + " ! Report this to the developer.");
        return null;
    }
}
