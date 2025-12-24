package xyz.propsik.punishments.Storage;

import xyz.propsik.punishments.Punishment;
import xyz.propsik.punishments.Util.Utils;

import java.sql.*;
import java.util.UUID;

public class h2DbManager implements DatabaseManager{
    private Connection connection;

    @Override
    public void connect() {
        try {
            Class.forName("org.h2.Driver");
            String url = "jdbc:h2:"+Utils.getInstance().getDataFolder().getAbsolutePath()+"/"+Utils.getConfig().getString("database.name");
            connection = DriverManager.getConnection(url);
        }
        catch (SQLException e) {
            Utils.getInstance().getLogger().severe("Error connecting to H2 database: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        return connection;
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
        try {
            var statement = getConnection().prepareStatement(sql);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            Utils.getInstance().getLogger().severe("Error creating SQL table: " + e.getMessage());
        }
    }
    @Override
    public void registerPunishment(UUID userId, String ipAddress, String punishmentType, String reason, UUID issuerId, String issuerName, long issuedAt, long expiresAt) {
        String sql = "INSERT INTO punishments (user_id, ip_address, punishment_type, reason, issuer, issuer_name, issued_at, expires_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            var statement = getConnection().prepareStatement(sql);
            statement.setString(1, userId.toString());
            statement.setString(2, ipAddress);
            statement.setString(3, punishmentType);
            statement.setString(4, reason);
            statement.setString(5, issuerId != null ? issuerId.toString() : null);
            statement.setString(6, issuerName);
            statement.setLong(7, issuedAt);
            if (expiresAt <= 0) {
                statement.setNull(8, Types.BIGINT);
            } else {
                statement.setLong(8, expiresAt);
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            Utils.getInstance().getLogger().severe("Error registering punishment: " + e.getMessage());
        }

    }
    @Override
    public void revokePunishment(int punishmentId, UUID revokedBy, long revokedAt, String revokedReason) {
        // to be implemented
    }
    @Override
    public Punishment getPunishmentById(int id) {
        try {
            String sql = "SELECT * FROM punishments WHERE id = ? LIMIT 1";
            var statement = getConnection().prepareStatement(sql);
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return new Punishment(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("user_id")),
                        rs.getString("ip_address"),
                        rs.getString("punishment_type"),
                        rs.getString("reason"),
                        rs.getString("issuer") != null ? UUID.fromString(rs.getString("issuer")) : null,
                        rs.getString("issuer_name"),
                        rs.getLong("issued_at"),
                        rs.getObject("expires_at") != null ? rs.getLong("expires_at") : null,
                        rs.getBoolean("active")
                );
            }
        } catch (SQLException e) {
            Utils.getInstance().getLogger().severe("Error retrieving punishment: " + e.getMessage());
        }
        return null;
    }
}
