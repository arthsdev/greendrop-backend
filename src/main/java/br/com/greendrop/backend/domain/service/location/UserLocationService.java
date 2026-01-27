package br.com.greendrop.backend.domain.service.location;

import br.com.greendrop.backend.domain.model.User;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLocationService {

    private final CurrentUserService currentUserService;
    private final UserLocationRepository userLocationRepository;
    private final UserLocationMapper userLocationMapper;

    // ============================================================
    // UPSERT LOCATION FOR LOGGED USER
    // ============================================================

    /**
     * Inserts or updates the location of the currently authenticated user.
     * Safe against concurrent requests and idempotent.
     *
     * @param dto latitude and longitude
     * @return the saved location as a response DTO
     */
    @Transactional
    public UserLocationResponseDTO upsertMyLocation(UserLocationRequestDTO dto) {
        User user = currentUserService.getCurrentUser();

        log.debug("Upserting location for user {}", user.getId());

        userLocationRepository.upsertLocation(user.getId(), dto.latitude(), dto.longitude());

        UserLocation saved = userLocationRepository.findByUser(user)
                .orElseThrow(() -> new UserLocationNotFoundException(user.getId()));

        log.debug("Location saved for user {} (lat={}, lon={})",
                user.getId(), saved.getLatitude(), saved.getLongitude());

        return userLocationMapper.toResponse(saved);
    }

    // ============================================================
    // GET LOCATION FOR LOGGED USER
    // ============================================================

    /**
     * Retrieves the location of the currently authenticated user, if exists.
     *
     * @return optional response DTO with the user's location
     */
    @Transactional(readOnly = true)
    public Optional<UserLocationResponseDTO> getMyLocation() {
        User user = currentUserService.getCurrentUser();

        return userLocationRepository.findByUser(user)
                .map(userLocationMapper::toResponse);
    }
}
