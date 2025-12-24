package xyz.propsik.punishments.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import xyz.propsik.punishments.Punishment;
import xyz.propsik.punishments.Util.Utils;

public class GetTestRecordCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Punishment punishment = Utils.getDatabaseManager().getPunishmentById(1);
        if(punishment != null) {
            commandSender.sendMessage("Punishment ID: " + punishment.getId());
            commandSender.sendMessage("Type: " + punishment.getType());
            commandSender.sendMessage("Reason: " + punishment.getReason());
            commandSender.sendMessage("Issued At: " + punishment.getIssuedAt());
            commandSender.sendMessage("Expires At: " + punishment.getExpiresAt());
        } else {
            commandSender.sendMessage("No punishment found with ID 1.");
        }
        return true;
    }
}
