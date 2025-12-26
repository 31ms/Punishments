package xyz.propsik.punishments;

import com.sun.tools.javac.Main;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.propsik.punishments.Commands.BanCommand;
import xyz.propsik.punishments.Commands.MainCommand;
import xyz.propsik.punishments.Listeners.AsyncPlayerPreLoginListener;
import xyz.propsik.punishments.Storage.H2DbManager;
import xyz.propsik.punishments.Storage.MySQLDbManager;
import xyz.propsik.punishments.Util.Messages;
import xyz.propsik.punishments.Util.Utils;

import java.util.Objects;


public final class Punishments extends JavaPlugin {
    @Override
    public void onEnable() {

        Utils.init(this);

        makeConfig();
        prepareDatabase();
        connectToDatabase();

        Messages.init();

        registerCommands();
        registerListeners();

    }
    private void registerCommands()
    {
        registerCommand("ban", new BanCommand());
        registerCommand("punishments", new MainCommand());
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
    private void registerListener(Listener listener)
    {
        getServer().getPluginManager().registerEvents(listener, this);
    }
    private void registerListeners()
    {
        registerListener(new AsyncPlayerPreLoginListener());
    }


    private void prepareDatabase()
    {
        String database = getConfig().getString("database.provider");
        if(database != null) database = database.toLowerCase();
        switch(database)
        {
            case "h2":
                Utils.setDatabaseManager(new H2DbManager());
                break;
            case "mysql":
                Utils.setDatabaseManager(new MySQLDbManager());
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
