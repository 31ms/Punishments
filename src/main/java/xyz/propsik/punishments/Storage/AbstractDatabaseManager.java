package xyz.propsik.punishments.Storage;

import org.jetbrains.annotations.Nullable;
import xyz.propsik.punishments.Punishment;
import xyz.propsik.punishments.Util.Utils;

import java.sql.*;
import java.util.UUID;

public abstract class AbstractDatabaseManager implements DatabaseManager {

    protected Connection connection;

    protected abstract String getJdbcUrl();
    protected abstract String getDriverClass();
    protected abstract void onConnectionError(Exception e);

    @Override
    public void connect() {
        try {
            Class.forName(getDriverClass());
            connection = DriverManager.getConnection(getJdbcUrl());
        } catch (Exception e) {
            Utils.getInstance().getLogger().severe("Database connection failed: " + e.getMessage());
            onConnectionError(e);
        }
    }



    @Override
    public void createTables() {
        String sql = """
            CREATE TABLE IF NOT EXISTS punishments (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id VARCHAR(36) NOT NULL,
                ip_address VARCHAR(45),
                punishment_type VARCHAR(50) NOT NULL,
                reason TEXT,
                issuer VARCHAR(36),
                issuer_name VARCHAR(16),
                issued_at BIGINT NOT NULL,
                expires_at BIGINT,
                active BOOLEAN DEFAULT TRUE,
                revoked_by VARCHAR(36),
                revoked_at BIGINT,
                revoked_reason TEXT
            );
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            Utils.getInstance().getLogger().severe("Error creating tables: " + e.getMessage());
            onConnectionError(e);
        }
    }

    @Override
    public void registerPunishment(
            UUID userId,
            String ipAddress,
            String punishmentType,
            String reason,
            UUID issuerId,
            String issuerName,
            long issuedAt,
            long expiresAt
    ) {
        String sql = """
            INSERT INTO punishments
            (user_id, ip_address, punishment_type, reason, issuer, issuer_name, issued_at, expires_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId.toString());
            stmt.setString(2, ipAddress);
            stmt.setString(3, punishmentType);
            stmt.setString(4, reason);
            stmt.setString(5, issuerId != null ? issuerId.toString() : null);
            stmt.setString(6, issuerName);
            stmt.setLong(7, issuedAt);

            if (expiresAt <= 0) {
                stmt.setNull(8, Types.BIGINT);
            } else {
                stmt.setLong(8, expiresAt);
            }

            stmt.executeUpdate();
        } catch (SQLException e) {
            Utils.getInstance().getLogger().severe("Error registering punishment: " + e.getMessage());
        }
    }

    @Override
    public void revokePunishment(int punishmentId, UUID revokedBy, long revokedAt, String revokedReason) {
        String sql = """
            UPDATE punishments
            SET active = FALSE, revoked_by = ?, revoked_at = ?, revoked_reason = ?
            WHERE id = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, revokedBy != null ? revokedBy.toString() : null);
            stmt.setLong(2, revokedAt);
            stmt.setString(3, revokedReason);
            stmt.setInt(4, punishmentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Utils.getInstance().getLogger().severe("Error revoking punishment: " + e.getMessage());
        }
    }

    @Override
    public @Nullable Punishment getPunishmentById(int id) {
        String sql = "SELECT * FROM punishments WHERE id = ? LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Punishment(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("user_id")),
                        rs.getString("ip_address"),
                        rs.getString("punishment_type"),
                        rs.getString("reason"),
                        rs.getString("issuer") != null
                                ? UUID.fromString(rs.getString("issuer"))
                                : null,
                        rs.getString("issuer_name"),
                        rs.getLong("issued_at"),
                        rs.getObject("expires_at") != null
                                ? rs.getLong("expires_at")
                                : null,
                        rs.getBoolean("active")
                );
            }
        } catch (SQLException e) {
            Utils.getInstance().getLogger().severe("Error retrieving punishment: " + e.getMessage());
        }

        return null;
    }
    public Punishment getBan(UUID userId) {
        String sql = "SELECT * FROM punishments WHERE user_id = ? AND punishment_type = 'BAN' AND active = TRUE LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Punishment(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("user_id")),
                        rs.getString("ip_address"),
                        rs.getString("punishment_type"),
                        rs.getString("reason"),
                        rs.getString("issuer") != null
                                ? UUID.fromString(rs.getString("issuer"))
                                : null,
                        rs.getString("issuer_name"),
                        rs.getLong("issued_at"),
                        rs.getObject("expires_at") != null
                                ? rs.getLong("expires_at")
                                : null,
                        rs.getBoolean("active")
                );
            }
        } catch (SQLException e) {
            Utils.getInstance().getLogger().severe("Error retrieving ban: " + e.getMessage());
        }

        return null;
    }
}
