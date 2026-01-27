package br.com.greendrop.backend.domain.repository;

import br.com.greendrop.backend.domain.model.UserLocation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for user locations.
 *
 * All queries are designed to avoid N+1 problems by using user IDs
 * instead of passing the full User object.
 * Also provides an atomic upsert for safe location updates.
 */
public interface UserLocationRepository extends JpaRepository<UserLocation, UUID> {

    // ---------------------------
    // SINGLE USER QUERIES
    // ---------------------------

    /**
     * Get the location for a single user by ID.
     * Using userId prevents unnecessary database queries on the User entity.
     */
    Optional<UserLocation> findByUserId(UUID userId);

    // ---------------------------
    // BATCH FETCH (MULTIPLE USERS)
    // ---------------------------

    /**
     * Get locations for a list of users.
     * EntityGraph makes sure the User object is loaded in the same query.
     * Useful to avoid N+1 when loading multiple locations.
     */
    @EntityGraph(attributePaths = {"user"})
    List<UserLocation> findAllByUserIdIn(List<UUID> userIds);

    // ---------------------------
    // UPSERT LOCATION
    // ---------------------------

    /**
     * Insert a new location or update the existing one for a user.
     * Uses native SQL UPSERT to make it safe for concurrent requests.
     *
     * @param userId UUID of the user
     * @param latitude latitude coordinate
     * @param longitude longitude coordinate
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
