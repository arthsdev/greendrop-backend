package br.com.greendrop.backend.domain.service.location;

import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.domain.model.UserLocation;
import br.com.greendrop.backend.domain.repository.UserLocationRepository;
import br.com.greendrop.backend.dto.location.UserLocationRequestDTO;
import br.com.greendrop.backend.dto.location.UserLocationResponseDTO;
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

    @Transactional
    public UserLocationResponseDTO upsertMyLocation(UserLocationRequestDTO dto) {
        User user = currentUserService.getCurrentUser();

        UserLocation location = userLocationRepository
                .findByUser(user)
                .orElseGet(() -> {
                    log.debug("Creating new location for user {}", user.getId());
                    return UserLocation.builder()
                            .user(user)
                            .build();
                });

        location.updateCoordinates(dto.latitude(), dto.longitude());

        UserLocation saved = userLocationRepository.save(location);

        log.debug(
                "Location saved for user {} (lat={}, lon={})",
                user.getId(),
                saved.getLatitude(),
                saved.getLongitude()
        );

        return userLocationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Optional<UserLocationResponseDTO> getMyLocation() {
        User user = currentUserService.getCurrentUser();

        Optional<UserLocation> location = userLocationRepository.findByUser(user);

        if (location.isEmpty()) {
            log.debug("User {} has no location registered", user.getId());
        }

        return location.map(userLocationMapper::toResponse);
    }
}
