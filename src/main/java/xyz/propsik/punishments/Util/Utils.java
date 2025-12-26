package xyz.propsik.punishments.Util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.propsik.punishments.Storage.DatabaseManager;

import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public abstract class Utils {
    static DatabaseManager databaseManager;
    private static JavaPlugin plugin;
    private static MiniMessage miniMessage;
    private static BukkitAudiences adventure;
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Pattern UUID_PATTERN =
            Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");
    public static void init(JavaPlugin pl) {
        plugin = pl;
        miniMessage = MiniMessage.miniMessage();
        adventure = BukkitAudiences.create(plugin);
    }
    public static JavaPlugin getInstance() {
        return plugin;
    }
    public static void setDatabaseManager(DatabaseManager dbm) {
        databaseManager = dbm;
    }
    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    public static FileConfiguration getConfig() {
        return plugin.getConfig();
    }
    public static MiniMessage getMiniMessage() {
        return miniMessage;
    }
    public static Component color(String message) {
        return getMiniMessage().deserialize(message);
    }
    public static void sendMessage(CommandSender sender, String message) {
        adventure.sender(sender).sendMessage(color(message));
    }
    public static void sendMessage(Player p, String message) {
        adventure.player(p).sendMessage(color(message));
    }
    public static CompletableFuture<UUID> fetchUUID(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + name))
                        .GET()
                        .build();

                HttpResponse<String> response =
                        HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    return null;
                }

                JsonObject json = JsonParser
                        .parseString(response.body())
                        .getAsJsonObject();

                if (!json.has("id")) {
                    return null;
                }

                String rawUuid = json.get("id").getAsString();
                return UUID.fromString(
                        UUID_PATTERN.matcher(rawUuid).replaceFirst("$1-$2-$3-$4-$5")
                );

            } catch (Exception e) {
                Utils.getInstance().getLogger().warning(
                        "Failed to fetch UUID for " + name + ": " + e.getMessage()
                );
                return null;
            }
        });
    }
    public static void ban(UUID uuid, InetAddress ip, String punishmentType, String reason, UUID issuerId, String issuerName, long issuedAt, long expiresAt)
    {
        getDatabaseManager().registerPunishment(uuid,
                ip != null ? ip.getHostAddress() : null,
                punishmentType,
                reason,
                issuerId,
                issuerName,
                issuedAt,
                expiresAt);
        Player p = Bukkit.getPlayer(uuid);
        String rs = reason != null ? reason : Messages.get("default-reason");
        if(Messages.get("default-issuer-name") == null)
        {
            Utils.getInstance().getLogger().warning("Default issuer name is not set in messages.yml");
        }
        String issuer = issuerName == null ? Messages.get("default-issuer-name") : issuerName;
        String dateFormat = Utils.getConfig().getString("date-format");
        if(dateFormat == null || dateFormat.isEmpty()) {
            dateFormat = "yyyy-MM-dd HH:mm";
        }
        String issuedAt2 = Instant.ofEpochMilli(issuedAt).atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ofPattern(dateFormat));
        String expiresIn = (expiresAt == -1) ? "Never" : Utils.formatDurationtoString(expiresAt - System.currentTimeMillis());
        String expiresAt2 = (expiresAt == -1) ? "Never" : Instant.ofEpochMilli(expiresAt).atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ofPattern(dateFormat));

        if(p != null && p.isOnline()) {
            String message = Messages.getList("ban-message").stream()
                    .map(line -> line
                            .replace("%reason%", rs)
                            .replace("%issuer%", issuer != null ? issuer : Messages.get("default-issuer-name"))
                            .replace("%issued_at%", issuedAt2)
                            .replace("%expires_in%", expiresIn)
                            .replace("%expires_at%", expiresAt2)
                            .replace("%punishment_id%", String.valueOf(getDatabaseManager().getBan(uuid).getId()))
                    )
                    .collect(Collectors.joining("\n"));
            p.kickPlayer(message);
        }
    }
    public static void broadcastPunishment(String issuer, String target, String reason, PunishmentType type, boolean isSilent)
    {
        String message;
        if(reason == null) {
            reason = Messages.get("default-reason");
        }
        switch(type) {
            case BAN -> message = isSilent ?
                    Messages.get("ban.broadcast-silent")
                            .replace("%issuer%", issuer)
                            .replace("%target%", target)
                            .replace("%reason%", reason)
                            .replace("%silent-prefix%", Messages.get("silent-prefix"))
                    :
                    Messages.get("ban.broadcast")
                            .replace("%issuer%", issuer)
                            .replace("%target%", target)
                            .replace("%reason%", reason);
            case MUTE -> message = isSilent ?
                    Messages.get("mute.broadcast-silent")
                            .replace("%issuer%", issuer)
                            .replace("%target%", target)
                            .replace("%reason%", reason)
                            .replace("%silent-prefix%", Messages.get("silent-prefix"))
                    :
                    Messages.get("mute.broadcast")
                            .replace("%issuer%", issuer)
                            .replace("%target%", target)
                            .replace("%reason%", reason);
            case WARN -> message = isSilent ?
                    Messages.get("warn.broadcast-silent")
                            .replace("%issuer%", issuer)
                            .replace("%target%", target)
                            .replace("%reason%", reason)
                            .replace("%silent-prefix%", Messages.get("silent-prefix"))
                    :
                    Messages.get("warn.broadcast")
                            .replace("%issuer%", issuer)
                            .replace("%target%", target)
                            .replace("%reason%", reason);
            case KICK -> message = isSilent ?
                    Messages.get("kick.broadcast-silent")
                            .replace("%issuer%", issuer)
                            .replace("%target%", target)
                            .replace("%reason%", reason)
                            .replace("%silent-prefix%", Messages.get("silent-prefix"))
                    :
                    Messages.get("kick.broadcast")
                            .replace("%issuer%", issuer)
                            .replace("%target%", target)
                            .replace("%reason%", reason);
            default -> message = "";
        }
        if(isSilent)
        {
            for(Player p : Bukkit.getOnlinePlayers())
            {
                if(p.hasPermission("punishments.notify.silent"))
                {
                    Utils.sendMessage(p, message);
                    Utils.getInstance().getLogger().info(issuer + "banned " + target + " silently " + "for reason: " + reason);
                }
            }
        }
        else {
            for(Player p : Bukkit.getOnlinePlayers())
            {
                Utils.sendMessage(p, message);
                Utils.getInstance().getLogger().info(issuer + "banned " + target + " for reason: " + reason);
            }
        }
    }
    public static void broadcastPunishment(String issuer, String target, String reason, TempPunishmentType type, long expiry, boolean isSilent)
    {
        String message;
        if(reason == null) {
            reason = Messages.get("default-reason");
        }
        switch(type) {
            case TEMPBAN -> message = isSilent ?
                    Messages.get("tempban.broadcast-silent")
                            .replace("%issuer%", issuer)
                            .replace("%target%", target)
                            .replace("%expiry%", formatDurationtoString(expiry))
                            .replace("%reason%", reason)
                            .replace("%silent-prefix%", Messages.get("silent-prefix"))
                    :
                    Messages.get("tempban.broadcast")
                            .replace("%issuer%", issuer)
                            .replace("%target%", target)
                            .replace("%expiry%", formatDurationtoString(expiry))
                            .replace("%reason%", reason);
            case TEMPMUTE -> message = isSilent ?
                    Messages.get("tempmute.broadcast-silent")
                            .replace("%issuer%", issuer)
                            .replace("%target%", target)
                            .replace("%expiry%", formatDurationtoString(expiry))
                            .replace("%reason%", reason)
                            .replace("%silent-prefix%", Messages.get("silent-prefix"))
                    :
                    Messages.get("tempmute.broadcast")
                            .replace("%issuer%", issuer)
                            .replace("%target%", target)
                            .replace("%expiry%", formatDurationtoString(expiry))
                            .replace("%reason%", reason);
            case TEMPWARN -> message = isSilent ?
                    Messages.get("tempwarn.broadcast-silent")
                            .replace("%issuer%", issuer)
                            .replace("%target%", target)
                            .replace("%expiry%", formatDurationtoString(expiry))
                            .replace("%reason%", reason)
                            .replace("%silent-prefix%", Messages.get("silent-prefix"))
                    :
                    Messages.get("tempwarn.broadcast")
                            .replace("%issuer%", issuer)
                            .replace("%target%", target)
                            .replace("%expiry%", formatDurationtoString(expiry))
                            .replace("%reason%", reason);
            default -> message = "";

        }
        if(isSilent)
        {
            for(Player p : Bukkit.getOnlinePlayers())
            {
                if(p.hasPermission("punishments.notify.silent"))
                {
                    Utils.sendMessage(p, message);
                }
            }
        }
        else {
            for(Player p : Bukkit.getOnlinePlayers())
            {
                Utils.sendMessage(p, message);
            }
        }
    }
    public static String formatDurationtoString(Long duration) {
        if(duration == null)
        {
            return "Never";
        }
        long minutes = duration / (1000 * 60) % 60;
        long hours = duration / (1000 * 60 * 60) % 24;
        long days = duration / (1000 * 60 * 60 * 24);
        long months = days / 28;
        days = days % 28;
        long years = months / 12;
        months = months % 12;

        StringBuilder sb = new StringBuilder();
        if (years > 0) sb.append(years).append(" ").append(getConfig().getString("duration-format.years")).append(" ");
        if (months > 0) sb.append(months).append(" ").append(getConfig().getString("duration-format.months")).append(" ");
        if (days > 0) sb.append(days).append(" ").append(getConfig().getString("duration-format.days")).append(" ");
        if (hours > 0) sb.append(hours).append(" ").append(getConfig().getString("duration-format.hours")).append(" ");
        if (minutes > 0 || sb.isEmpty()) sb.append(minutes).append(" ").append(getConfig().getString("duration-format.minutes"));

        return sb.toString().trim();
    }


}
