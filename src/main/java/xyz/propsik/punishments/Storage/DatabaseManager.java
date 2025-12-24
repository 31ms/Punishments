package xyz.propsik.punishments.Storage;

import xyz.propsik.punishments.Punishment;

import java.util.UUID;

public interface DatabaseManager {
    void connect();
    void createTables();
    void registerPunishment(UUID userId, String ipAddress, String punishmentType, String reason, UUID issuerId, String issuerName, long issuedAt, long expiresAt);
    void revokePunishment(int punishmentId, UUID revokedBy, long revokedAt, String revokedReason);
    Punishment getPunishmentById(int punishmentId);
}
