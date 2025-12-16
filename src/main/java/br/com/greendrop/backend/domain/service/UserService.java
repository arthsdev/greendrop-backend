package br.com.greendrop.backend.domain.service;

import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.domain.repository.UserRepository;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    // ============================================================
    // ADMIN READ
    // ============================================================

    /**
     * Returns a paginated list of all users. Accessible for Admins.
     */
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    /**
     * Finds any user by ID. Admin-only endpoint.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO findById(UUID id) {
        return userMapper.toResponse(getUserOrThrow(id));
    }

    // ============================================================
    // LOGGED USER OPERATIONS (Standard Market Pattern)
    // ============================================================

    /**
     * Returns the profile of the currently authenticated user.
     * This method does NOT receive an ID because logged users
     * should not manually provide their UUID.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getLoggedUser() {
        User user = currentUserService.getCurrentUser();
        return userMapper.toResponse(user);
    }

    /**
     * Updates profile information of the authenticated user.
     * No ID is required; it's resolved through JWT.
     */
    @Transactional
    public UserResponseDTO updateUserProfile(UserUpdateDTO dto) {
        User user = currentUserService.getCurrentUser();

        // Partial update handled by MapStruct decorator
        userMapper.updateEntity(dto, user);

        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    /**
     * Updates the authenticated user's password after validating the old password.
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
     * Deletes a user by ID. Accessible only to admins.
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
     * Retrieves a user by ID or throws an exception.
     */
    private User getUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }
}