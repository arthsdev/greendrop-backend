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
 * Entity representing a user's geographic location.
 * This entity stores latitude and longitude coordinates for a specific user.
 * Coordinates are validated to ensure they are within acceptable ranges:
 * latitude [-90, 90] and longitude [-180, 180].
 *
 * Automatic timestamps are managed with {@link PrePersist} and {@link PreUpdate}
 * hooks, storing {@code createdAt} and {@code updatedAt} in microsecond precision.
 *
 * Domain rules enforce validation via {@link BusinessException} with appropriate
 * {@link ErrorCode} values.
 *
 * Logging is performed during creation, update, and coordinate changes to allow
 * debugging and monitoring of location updates.
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

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * Updates the coordinates for the user location.
     */
    public void updateCoordinates(Double novaLatitude, Double novaLongitude) {
        validateCoordinates(novaLatitude, novaLongitude);
        this.latitude = novaLatitude;
        this.longitude = novaLongitude;

        log.debug("[UserLocation] User {} coordinates updated: lat={}, lon={}",
                user.getId(), this.latitude, this.longitude);
    }

    /**
     * Validates the given latitude and longitude.
     */
    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new BusinessException(ErrorCode.USER_LOCATION_INVALID_LATITUDE);
        }
        if (longitude < -180 || longitude > 180) {
            throw new BusinessException(ErrorCode.USER_LOCATION_INVALID_LONGITUDE);
        }
    }

    /**
     * JPA callback triggered before persisting a new location.
     * Sets both createdAt and updatedAt to the current timestamp.
     */
    @PrePersist
    public void prePersist() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        this.createdAt = now;
        this.updatedAt = now;

        log.debug("[UserLocation] New location will be persisted: user={}, lat={}, lon={}",
                user.getId(), latitude, longitude);
    }

    /**
     * JPA callback triggered before updating an existing location.
     * Updates the updatedAt timestamp to the current time.
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);

        log.debug("[UserLocation] Location updated: user={}, lat={}, lon={}",
                user.getId(), latitude, longitude);
    }
}
