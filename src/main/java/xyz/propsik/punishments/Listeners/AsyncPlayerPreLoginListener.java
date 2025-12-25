package xyz.propsik.punishments.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import xyz.propsik.punishments.Punishment;
import xyz.propsik.punishments.Util.Messages;
import xyz.propsik.punishments.Util.Utils;

import java.util.stream.Collectors;


public class AsyncPlayerPreLoginListener implements Listener {
    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        Punishment punishment = Utils.getDatabaseManager().getBan(event.getUniqueId());
        if(!(punishment == null)) {
            if(punishment.hasExpired())
            {
                Utils.getDatabaseManager().revokePunishment(
                        punishment.getId(),
                        null,
                        System.currentTimeMillis(),
                        "Automatic unban - ban has expired"
                );
                return;
            }
            String reason = punishment.getReason() != null ? punishment.getReason() : Messages.get("default-reason");
            String issuer = punishment.getIssuerName() != null ? punishment.getIssuerName() : "CONSOLE";
            String issuedAt = Utils.formatDurationtoString(punishment.getIssuedAt());
            String expiresAt = (punishment.getExpiresAt() == null) ? "Never" : Utils.formatDurationtoString(punishment.getExpiresAt() - System.currentTimeMillis());

            String message = Messages.getList("ban-message").stream()
                    .map(line -> line
                            .replace("%reason%", reason)
                            .replace("%issuer%", issuer)
                            .replace("%issued_at%", issuedAt)
                            .replace("%expires_in%", expiresAt)
                    )
                    .collect(Collectors.joining("\n"));
            event.disallow(Result.KICK_BANNED, message);
        }
    }
}
