package xyz.propsik.punishments.Storage;

import org.bukkit.Bukkit;
import xyz.propsik.punishments.Util.Utils;

import java.sql.DriverManager;

public class MySQLDbManager extends AbstractDatabaseManager {

    @Override
    protected String getJdbcUrl() {
        return "jdbc:mysql://" +
                Utils.getConfig().getString("database.host") + "/" +
                Utils.getConfig().getString("database.name");
    }

    @Override
    protected String getDriverClass() {
        return "com.mysql.cj.jdbc.Driver";
    }

    @Override
    protected void onConnectionError(Exception e) {
        Bukkit.getServer().getPluginManager().disablePlugin(Utils.getInstance());
    }

    @Override
    public void connect() {
        try {
            Class.forName(getDriverClass());
            connection = DriverManager.getConnection(
                    getJdbcUrl(),
                    Utils.getConfig().getString("database.username"),
                    Utils.getConfig().getString("database.password")
            );
        } catch (Exception e) {
            Utils.getInstance().getLogger().severe("MySQL connection failed: " + e.getMessage());
            onConnectionError(e);
        }
    }
}
