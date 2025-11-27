package br.com.greendrop.backend.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;
import java.io.Serial;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents an access or refresh token stored in Redis.
 * Fields are consistent with Redis storage, and derived properties are ignored for JSON serialization.
 * Ensures safe storage and retrieval with Jackson2JsonRedisSerializer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore any unknown fields in JSON
public class Token implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Owner of the token (UUID of User entity).
     */
    private UUID userId;

    /**
     * JWT token string.
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

    // =====================================================================
    // Derived properties (not stored in Redis as fields)
    // =====================================================================

    /**
     * Checks whether the token is expired.
     *
     * @return true if expiresAt is null or current time is after expiresAt
     */
    @JsonIgnore // Prevent Jackson from serializing this property
    public boolean isExpired() {
        return expiresAt == null || Instant.now().isAfter(expiresAt);
    }

    /**
     * Returns the remaining TTL (time-to-live) in seconds.
     *
     * @return remaining seconds until token expiration, 0 if expired
     */
    @JsonIgnore // Prevent Jackson from serializing this property
    public long getRemainingTTLSeconds() {
        if (expiresAt == null) return 0;
        return Math.max(0, expiresAt.getEpochSecond() - Instant.now().getEpochSecond());
    }
}
