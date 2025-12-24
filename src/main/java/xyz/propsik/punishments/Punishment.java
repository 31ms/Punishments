package xyz.propsik.punishments;

import java.util.UUID;

public class Punishment {

    private final int id;
    private final UUID userId;
    private final String ipAddress;
    private final String type;
    private final String reason;
    private final UUID issuer;
    private final String issuerName;
    private final long issuedAt;
    private final Long expiresAt; // nullable
    private final boolean active;

    public Punishment(
            int id,
            UUID userId,
            String ipAddress,
            String type,
            String reason,
            UUID issuer,
            String issuerName,
            long issuedAt,
            Long expiresAt,
            boolean active
    ) {
        this.id = id;
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.type = type;
        this.reason = reason;
        this.issuer = issuer;
        this.issuerName = issuerName;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.active = active;
    }

    public boolean hasExpired() {
        return expiresAt != null && System.currentTimeMillis() > expiresAt;
    }

    public boolean isPermanent() {
        return expiresAt == null;
    }

    public int getId() {
        return id;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public long getIssuedAt() {
        return issuedAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public String getReason() {
        return reason;
    }

    public String getType() {
        return type;
    }

    public UUID getIssuer() {
        return issuer;
    }

    public UUID getUserId() {
        return userId;
    }
    public boolean isActive() {
        return active;
    }
}
