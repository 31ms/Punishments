package xyz.propsik.punishments.Util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.propsik.punishments.Storage.DatabaseManager;

public abstract class Utils {
    static DatabaseManager databaseManager;
    private static JavaPlugin plugin;
    public static void init(JavaPlugin pl) {
        plugin = pl;
    }
    public static JavaPlugin getInstance() {
        return plugin;
    }
    public static void setDatabaseManager(DatabaseManager dbm) {
        databaseManager = dbm;
    }
    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    public static FileConfiguration getConfig() {
        return plugin.getConfig();
    }

}
