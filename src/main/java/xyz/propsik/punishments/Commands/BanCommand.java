package xyz.propsik.punishments.Commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
        if (strings.length == 0) return false;
        if (strings.length > 3) return false;

        Player onlinePlayer = Bukkit.getPlayerExact(strings[0]);

        if (onlinePlayer != null) {
            runBanAsync(commandSender, onlinePlayer.getUniqueId(), onlinePlayer, strings);
        } else {
            Utils.fetchUUID(strings[0]).thenAccept(uuid -> {
                if (uuid == null) {
                    Bukkit.getScheduler().runTask(Utils.getInstance(), () ->
                            Utils.sendMessage(commandSender,
                                    Messages.get("ban.player-not-found").replace("%target%", strings[0]))
                    );
                    return;
                }
                runBanAsync(commandSender, uuid, null, strings);
            });
        }

        return true;
    }

    private void runBanAsync(CommandSender sender, UUID uuid, @Nullable Player player, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(Utils.getInstance(), () -> {
            if (Utils.getDatabaseManager().getBan(uuid) != null) {
                Bukkit.getScheduler().runTask(Utils.getInstance(), () ->
                        Utils.sendMessage(sender,
                                Messages.get("ban.already-banned").replace("%target%", args[0]))
                );
                return;
            }

            UUID tempIssuerId = null;
            String tempIssuerName = Utils.getConfig().getString("default-issuer-name");
            if (sender instanceof Player p) {
                tempIssuerId = p.getUniqueId();
                tempIssuerName = p.getName();
            }
            if (tempIssuerName == null) tempIssuerName = "Console";

            final UUID issuerId = tempIssuerId;
            final String issuerName = tempIssuerName;

            Bukkit.getScheduler().runTask(Utils.getInstance(), () -> {
                Utils.ban(uuid,
                        (player != null && player.isOnline()) ? Objects.requireNonNull(player.getAddress()).getAddress() : null,
                        "BAN",
                        getReason(args),
                        issuerId,
                        issuerName,
                        System.currentTimeMillis(),
                        -1
                );
            });

            final String targetName = (player != null) ? player.getName() : args[0];
            final String reason = getReason(args);

            Bukkit.getScheduler().runTask(Utils.getInstance(), () ->
                    Utils.broadcastPunishment(
                            issuerName,
                            targetName,
                            reason,
                            PunishmentType.BAN,
                            Arrays.asList(args).contains("-s")
                    )
            );
        });
    }




    private static @Nullable String getReason(String[] strings) {
        String reason;
        if(strings.length == 2 && !strings[1].equals("-s"))
        {
            reason = strings[1];
        }
        else if(strings.length == 3 && strings[2].equals("-s"))
        {
            reason = strings[1];
        }
        else if(strings.length == 3 && strings[1].equals("-s"))
        {
            reason = strings[2];
        }
        else {
            reason = null;
        }
        return reason;
    }
}
