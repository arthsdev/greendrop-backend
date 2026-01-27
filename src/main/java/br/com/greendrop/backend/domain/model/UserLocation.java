package br.com.greendrop.backend.domain.model;

import br.com.greendrop.backend.exception.generic.BusinessException;
import br.com.greendrop.backend.exception.global.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Represents a user's geographic location.
 * - Stores latitude and longitude.
 * - Validates coordinates.
 * - Timestamps are automatically managed via JPA callbacks.
 * - Designed to be fetched efficiently via userId to avoid N+1.
 */
@Entity
@Table(name = "user_location")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLocation {

    private static final Logger log = LoggerFactory.getLogger(UserLocation.class);

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The user associated with this location.
     * One-to-one mapping.
     */
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Latitude coordinate [-90, 90]
     */
    @Column(nullable = false)
    private Double latitude;

    /**
     * Longitude coordinate [-180, 180]
     */
    @Column(nullable = false)
    private Double longitude;

    /**
     * Creation timestamp (microseconds)
     */
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Last update timestamp (microseconds)
     */
    @Column(nullable = false)
    private Instant updatedAt;

    // ============================================================
    // BUSINESS METHODS
    // ============================================================

    /**
     * Update coordinates safely with validation.
     */
    public void updateCoordinates(Double latitude, Double longitude) {
        validateCoordinates(latitude, longitude);
        this.latitude = latitude;
        this.longitude = longitude;

        log.debug("[UserLocation] User {} coordinates updated: lat={}, lon={}",
                user.getId(), latitude, longitude);
    }

    /**
     * Ensures latitude and longitude are within valid ranges.
     */
    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new BusinessException(ErrorCode.USER_LOCATION_INVALID_LATITUDE);
        }
        if (longitude < -180 || longitude > 180) {
            throw new BusinessException(ErrorCode.USER_LOCATION_INVALID_LONGITUDE);
        }
    }

    // ============================================================
    // JPA CALLBACKS
    // ============================================================

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        this.createdAt = now;
        this.updatedAt = now;

        log.debug("[UserLocation] Persisting new location: user={}, lat={}, lon={}",
                user.getId(), latitude, longitude);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);

        log.debug("[UserLocation] Updating location: user={}, lat={}, lon={}",
                user.getId(), latitude, longitude);
    }
}
