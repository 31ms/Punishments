package xyz.propsik.punishments.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.propsik.punishments.Util.Messages;
import xyz.propsik.punishments.Util.Utils;

import java.util.List;

public class MainCommand implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(strings.length == 0) {
            Utils.sendMessage(commandSender, "<yellow>Punishments <gold>» <yellow>Version <gold>" + Utils.getInstance().getDescription().getVersion());
            Utils.sendMessage(commandSender, "<yellow>Punishments <gold>» <yellow>Developed by <gold>Propsik");
            return true;
        }
        if(strings.length == 1) {
            if(strings[0].equalsIgnoreCase("reload")) {
                Utils.getInstance().saveDefaultConfig();
                Utils.getInstance().reloadConfig();
                Messages.reload();
                Utils.sendMessage(commandSender, "<yellow>Punishments <gold>» <yellow>Reloaded configuration files.");
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(strings.length == 1) {
            return List.of("reload");
        } else {
            return List.of();
        }
    }
}
