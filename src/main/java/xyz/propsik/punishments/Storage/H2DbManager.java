package xyz.propsik.punishments.Storage;

import xyz.propsik.punishments.Util.Utils;

public class H2DbManager extends AbstractDatabaseManager {

    @Override
    protected String getJdbcUrl() {
        return "jdbc:h2:" +
                Utils.getInstance().getDataFolder().getAbsolutePath() +
                "/" + Utils.getConfig().getString("database.name");
    }

    @Override
    protected String getDriverClass() {
        return "org.h2.Driver";
    }

    @Override
    protected void onConnectionError(Exception e) {
        Utils.getInstance().getLogger().severe("Couldn't connect to the H2 database: "+e.getMessage());
        Utils.getInstance().getServer().getPluginManager().disablePlugin(Utils.getInstance());
    }
}
