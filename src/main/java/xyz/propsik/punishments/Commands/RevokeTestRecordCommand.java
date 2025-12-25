package xyz.propsik.punishments.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import xyz.propsik.punishments.Util.Utils;

import java.util.UUID;

public class RevokeTestRecordCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Utils.getDatabaseManager().revokePunishment(1, UUID.randomUUID(), System.currentTimeMillis(), "Revoked for testing purposes");
        return true;
    }
}
