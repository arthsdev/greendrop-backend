package br.com.greendrop.backend.domain.service;

import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.domain.repository.UserRepository;
import br.com.greendrop.backend.dto.pagination.PaginatedResponse;
import br.com.greendrop.backend.dto.pagination.PageMetaResponse;
import br.com.greendrop.backend.dto.user.UserResponseDTO;
import br.com.greendrop.backend.dto.user.UserUpdateDTO;
import br.com.greendrop.backend.exception.user.PasswordInvalidException;
import br.com.greendrop.backend.exception.user.UserNotFoundException;
import br.com.greendrop.backend.infrastructure.security.service.CurrentUserService;
import br.com.greendrop.backend.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service class responsible for User-related business logic.
 *
 * Best practices applied:
 * - Uses repository methods with @EntityGraph to prevent N+1.
 * - All read operations map entities to DTOs before returning to avoid lazy-loading issues.
 * - Transaction readOnly flag applied for read operations.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    // ============================================================
    // ADMIN READ (Paginated)
    // ============================================================

    /**
     * Returns all users paginated.
     * Uses findAllWithRelations to fetch roles/profile in one query (N+1 safe).
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<UserResponseDTO> findAll(Pageable pageable) {
        Page<User> page = userRepository.findAllWithRelations(pageable);

        List<UserResponseDTO> content = page.getContent()
                .stream()
                .map(userMapper::toResponse)
                .toList();

        PageMetaResponse meta = new PageMetaResponse(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );

        return new PaginatedResponse<>(meta, content);
    }

    /**
     * Returns a single user by ID.
     * Uses N+1 safe getUserOrThrow helper.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO findById(UUID id) {
        return userMapper.toResponse(getUserOrThrow(id));
    }

    // ============================================================
    // LOGGED USER OPERATIONS
    // ============================================================

    /**
     * Returns the currently logged-in user's data.
     * N+1 safe: loads user aggregate with required associations eagerly.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getLoggedUser() {
        UUID userId = currentUserService.getCurrentUserId();
        return userMapper.toResponse(getUserOrThrow(userId));
    }

    /**
     * Updates the profile of the currently logged-in user.
     * Only updates fields directly on User entity.
     */
    @Transactional
    public UserResponseDTO updateUserProfile(UserUpdateDTO dto) {
        User user = currentUserService.getCurrentUser();
        userMapper.updateEntity(dto, user);
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    /**
     * Updates the password of the currently logged-in user.
     * Validates old password before saving.
     */
    @Transactional
    public void updatePassword(String oldPassword, String newPassword) {
        User user = currentUserService.getCurrentUser();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new PasswordInvalidException();
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ============================================================
    // ADMIN DELETE
    // ============================================================

    /**
     * Deletes a user by ID.
     * Checks existence first to prevent unnecessary exceptions.
     */
    @Transactional
    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException();
        }
        userRepository.deleteById(id);
    }

    // ============================================================
    // INTERNAL HELPER
    // ============================================================

    /**
     * Helper to fetch a user by ID with all important relations.
     * Centralizes the "fetch or throw" pattern.
     * Prevents N+1 when mapping to DTO.
     */
    private User getUserOrThrow(UUID id) {
        return userRepository.findByIdWithRelations(id)
                .orElseThrow(UserNotFoundException::new);
    }
}
