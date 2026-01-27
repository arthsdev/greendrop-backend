package br.com.greendrop.backend.domain.repository;

import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.domain.model.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link UserLocation} entity.
 * <p>
 * Provides CRUD operations, as well as custom methods to handle user location updates
 * in an atomic and idempotent way.
 */
public interface UserLocationRepository extends JpaRepository<UserLocation, UUID> {

    /**
     * Finds the location of a given user, if it exists.
     */
    Optional<UserLocation> findByUser(User user);

    /**
     * Inserts a new location for the user or updates the existing location if already present.
     * This method uses a native SQL UPSERT (INSERT ... ON DUPLICATE KEY UPDATE) to ensure
     * atomicity and prevent race conditions when multiple requests try to update the location
     * at the same time.
     * - If the user has no existing location, a new row is inserted.
     * - If the user already has a location, the latitude, longitude, and updated_at are updated.
     */
    @Modifying
    @Query(value = """
        INSERT INTO user_location (
            id,
            user_id,
            latitude,
            longitude,
            created_at,
            updated_at
        ) VALUES (
            UNHEX(REPLACE(UUID(), '-', '')),
            :userId,
            :lat,
            :lon,
            NOW(),
            NOW()
        )
        ON DUPLICATE KEY UPDATE
            latitude = VALUES(latitude),
            longitude = VALUES(longitude),
            updated_at = NOW()
    """, nativeQuery = true)
    void upsertLocation(@Param("userId") UUID userId,
                        @Param("lat") Double latitude,
                        @Param("lon") Double longitude);
}
