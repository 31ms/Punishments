package xyz.propsik.punishments.Util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public abstract class Messages {
    private static FileConfiguration config;
    private static File configFile;
    public static void init() {
        File dataFolder = Utils.getInstance().getDataFolder();

        configFile = new File(dataFolder, "messages.yml");

        if (!configFile.exists()) {
            Utils.getInstance().saveResource("messages.yml", false);
        }

        reload();
    }
    public static String get(String path)
    {
        return config.getString(path);
    }
    public static void reload()
    {
        config = YamlConfiguration.loadConfiguration(configFile);
    }


}
