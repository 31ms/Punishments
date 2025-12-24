package xyz.propsik.punishments.Commands;

import xyz.propsik.punishments.Util.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class AddTestRecordCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Utils.getDatabaseManager().registerPunishment(UUID.randomUUID(), "127.0.0.1", "TEST", "This is a test record", UUID.randomUUID(), "Propsik_", System.currentTimeMillis(), System.currentTimeMillis() + 86400000);
        return true;
    }
}
