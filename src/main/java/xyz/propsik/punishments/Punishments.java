package xyz.propsik.punishments;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.propsik.punishments.Commands.AddTestRecordCommand;
import xyz.propsik.punishments.Commands.GetTestRecordCommand;
import xyz.propsik.punishments.Storage.h2DbManager;
import xyz.propsik.punishments.Util.Utils;

import java.util.Objects;


public final class Punishments extends JavaPlugin {
    @Override
    public void onEnable() {

        Utils.init(this);
        makeConfig();
        prepareDatabase();
        connectToDatabase();

        registerCommands();

    }
    private void registerCommands()
    {
        registerCommand("addtestrecord", new AddTestRecordCommand());
        registerCommand("gettestrecord", new GetTestRecordCommand());
    }
    private void registerCommand(String command, CommandExecutor executor)
    {
        Objects.requireNonNull(getCommand(command)).setExecutor(executor);
    }
    private void makeConfig()
    {
        if(!getDataFolder().exists()) {
            if(getDataFolder().mkdir())
            {
                getLogger().info("Created plugin folder");
            } else {
                getLogger().severe("Could not create plugin folder!");
            }
        }
        saveDefaultConfig();
    }
    private void prepareDatabase()
    {
        String database = getConfig().getString("database.provider");
        switch(database)
        {
            case "h2":
                Utils.setDatabaseManager(new h2DbManager());
                break;
            case null:
                getLogger().severe("No database type specified in config.yml!");
                getServer().getPluginManager().disablePlugin(this);
                break;
            default:
                getLogger().severe("Unsupported database type specified in config.yml!");
                getServer().getPluginManager().disablePlugin(this);
        }
    }
    public void connectToDatabase()
    {
        Utils.getDatabaseManager().connect();
        Utils.getDatabaseManager().createTables();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
