package br.com.greendrop.backend.domain.service.location;

import br.com.greendrop.backend.domain.model.UserLocation;
import br.com.greendrop.backend.domain.repository.UserLocationRepository;
import br.com.greendrop.backend.dto.location.UserLocationRequestDTO;
import br.com.greendrop.backend.dto.location.UserLocationResponseDTO;
import br.com.greendrop.backend.exception.user.UserLocationNotFoundException;
import br.com.greendrop.backend.infrastructure.security.service.CurrentUserService;
import br.com.greendrop.backend.mapper.location.UserLocationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service to handle user location operations.
 * Uses user IDs instead of full User objects to avoid N+1 issues.
 * Supports upsert (insert or update) for location safely.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserLocationService {

    private final CurrentUserService currentUserService;
    private final UserLocationRepository userLocationRepository;
    private final UserLocationMapper userLocationMapper;

    // -----------------------------
    // UPSERT LOCATION
    // -----------------------------

    /**
     * Insert or update the current user's location.
     * Safe for concurrent requests.
     *
     * @param dto contains latitude and longitude
     * @return the saved location as a DTO
     */
    @Transactional
    public UserLocationResponseDTO upsertMyLocation(UserLocationRequestDTO dto) {
        UUID userId = currentUserService.getCurrentUser().getId();

        log.debug("Upserting location for user {}", userId);

        // Upsert in DB (atomic)
        userLocationRepository.upsertLocation(userId, dto.latitude(), dto.longitude());

        // Fetch updated location by userId (avoids lazy loading / N+1)
        UserLocation saved = userLocationRepository.findByUserId(userId)
                .orElseThrow(() -> new UserLocationNotFoundException(userId));

        log.debug("Location saved for user {} (lat={}, lon={})",
                userId, saved.getLatitude(), saved.getLongitude());

        return userLocationMapper.toResponse(saved);
    }

    // -----------------------------
    // GET LOCATION
    // -----------------------------

    /**
     * Get the current user's location if it exists.
     *
     * @return optional DTO with location
     */
    @Transactional(readOnly = true)
    public Optional<UserLocationResponseDTO> getMyLocation() {
        UUID userId = currentUserService.getCurrentUser().getId();

        return userLocationRepository.findByUserId(userId)
                .map(userLocationMapper::toResponse);
    }
}
