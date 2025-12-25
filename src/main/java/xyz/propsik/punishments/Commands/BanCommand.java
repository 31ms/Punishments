package xyz.propsik.punishments.Commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.propsik.punishments.Util.Messages;
import xyz.propsik.punishments.Util.PunishmentType;
import xyz.propsik.punishments.Util.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;



public class BanCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
        if(strings.length == 0) { return false; }
        Utils.fetchUUID(strings[0]).thenAccept(uuid -> {
            if(uuid == null) {
                Utils.sendMessage(commandSender, Messages.get("ban.player-not-found").replace("%player%", strings[0]));
            }
            else {
                if(!(Utils.getDatabaseManager().getBan(uuid) == null)) {
                    Utils.sendMessage(commandSender, Messages.get("ban.already-banned").replace("%target%", strings[0]));
                }
                else {
                    Player player = Bukkit.getPlayer(uuid);
                    UUID issuerId;
                    String issuerName;
                    if (commandSender instanceof Player issuer) {
                        if (issuer.getUniqueId().equals(uuid)) {
                            Utils.sendMessage(issuer, Messages.get("ban.cant-ban-self"));
                            return;
                        }
                        issuerId = issuer.getUniqueId();
                        issuerName = issuer.getName();

                    } else {
                        issuerId = null;
                        issuerName = "CONSOLE";
                    }
                    if (player != null && player.isOnline()) {
                        Utils.ban(uuid, Objects.requireNonNull(player.getAddress()).getAddress(), "BAN", null, issuerId, issuerName, System.currentTimeMillis(), -1);
                    } else {
                        Utils.ban(uuid, null, "BAN", null, issuerId, issuerName, System.currentTimeMillis(), -1);
                    }
                    String targetName = (player != null) ? player.getName() : strings[0];
                    Bukkit.getScheduler().runTask(Utils.getInstance(), () -> Utils.broadcastPunishment(issuerName, targetName, null, PunishmentType.BAN, Arrays.asList(strings).contains("-s")));
                }
            }
        });
        return true;
    }
}
