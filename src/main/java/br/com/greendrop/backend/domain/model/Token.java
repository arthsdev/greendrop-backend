package br.com.greendrop.backend.domain.model;

import lombok.*;
import java.io.Serializable;
import java.io.Serial;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents an access or refresh token stored in Redis.
 * Keep field names consistent across repository/service layers.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Owner of the token (UUID of User entity).
     */
    private UUID userId;

    /**
     * Token string (JWT). Keep this name consistent across the codebase.
     */
    private String value;

    /**
     * Token type: "ACCESS" or "REFRESH".
     */
    private String type;

    /**
     * Token creation timestamp.
     */
    private Instant createdAt;

    /**
     * Token expiry timestamp.
     */
    private Instant expiresAt;

    /**
     * Helper to check expiration.
     */
    public boolean isExpired() {
        return expiresAt == null || Instant.now().isAfter(expiresAt);
    }

    /**
     * Remaining TTL in seconds (>= 0).
     */
    public long getRemainingTTLSeconds() {
        if (expiresAt == null) return 0;
        return Math.max(0, expiresAt.getEpochSecond() - Instant.now().getEpochSecond());
    }
}
