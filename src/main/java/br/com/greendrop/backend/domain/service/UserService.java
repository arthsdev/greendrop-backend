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

    @Transactional(readOnly = true)
    public PaginatedResponse<UserResponseDTO> findAll(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);

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

    @Transactional(readOnly = true)
    public UserResponseDTO findById(UUID id) {
        return userMapper.toResponse(getUserOrThrow(id));
    }

    // ============================================================
    // LOGGED USER OPERATIONS
    // ============================================================

    @Transactional(readOnly = true)
    public UserResponseDTO getLoggedUser() {
        User user = currentUserService.getCurrentUser();
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponseDTO updateUserProfile(UserUpdateDTO dto) {
        User user = currentUserService.getCurrentUser();
        userMapper.updateEntity(dto, user);
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

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

    private User getUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }
}
