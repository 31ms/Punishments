package xyz.propsik.punishments.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import xyz.propsik.punishments.Punishment;
import xyz.propsik.punishments.Util.Messages;
import xyz.propsik.punishments.Util.Utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;


public class AsyncPlayerPreLoginListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
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
            String issuer = punishment.getIssuerName() != null ? punishment.getIssuerName() : Utils.getConfig().getString("default-issuer-name");
            String dateFormat = Utils.getConfig().getString("date-format");
            if(dateFormat == null || dateFormat.isEmpty()) {
                dateFormat = "yyyy-MM-dd HH:mm";
            }
            String issuedAt = Instant.ofEpochMilli(punishment.getIssuedAt()).atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ofPattern(dateFormat));
            String expiresIn = (punishment.getExpiresAt() == null) ? "Never" : Utils.formatDurationtoString(punishment.getExpiresAt() - System.currentTimeMillis());
            String expiresAt = (punishment.getExpiresAt() == null) ? "Never" : Instant.ofEpochMilli(punishment.getExpiresAt()).atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ofPattern(dateFormat));

            String message = Messages.getList("ban-message").stream()
                    .map(line -> line
                            .replace("%reason%", reason)
                            .replace("%issuer%", issuer != null ? issuer : Messages.get("default-issuer-name"))
                            .replace("%issued_at%", issuedAt)
                            .replace("%expires_in%", expiresIn)
                            .replace("%punishment_id%", String.valueOf(punishment.getId()))
                            .replace("%expires_at%", expiresAt)
                    )
                    .collect(Collectors.joining("\n"));
            event.disallow(Result.KICK_BANNED, message);
        }
    }
}
