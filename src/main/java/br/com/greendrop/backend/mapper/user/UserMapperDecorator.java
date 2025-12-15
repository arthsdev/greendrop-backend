package br.com.greendrop.backend.mapper.user;

import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.dto.user.UserRequestDTO;
import br.com.greendrop.backend.dto.user.UserResponseDTO;
import br.com.greendrop.backend.dto.user.UserUpdateDTO;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Decorator responsible for adding custom logic
 * (sanitization + logging) on top of MapStruct.
 */
@Slf4j
@Setter
public abstract class UserMapperDecorator implements UserMapper {

    protected UserMapper delegate;

    // -----------------------------
    // Sanitization
    // -----------------------------
    private String sanitize(String value, String fieldName) {
        if (value == null) return null;

        String sanitized = value.trim().replaceAll("\\s+", " ");

        if (!value.equals(sanitized)) {
            log.debug(
                    "Sanitized field '{}' from '{}' to '{}'",
                    fieldName, value, sanitized
            );
        }

        return sanitized;
    }

    // -----------------------------
    // Entity → DTO
    // -----------------------------
    @Override
    public UserResponseDTO toResponse(User user) {
        return delegate.toResponse(user);
    }

    // -----------------------------
    // DTO → Entity
    // -----------------------------
    @Override
    public User toEntity(UserRequestDTO dto) {
        User user = delegate.toEntity(dto);

        user.setName(sanitize(user.getName(), "name"));
        user.setEmail(sanitize(user.getEmail(), "email"));
        user.setCep(sanitize(user.getCep(), "cep"));

        return user;
    }

    // -----------------------------
    // Update Entity (PATCH)
    // -----------------------------
    @Override
    public void updateEntity(UserUpdateDTO dto, User user) {
        delegate.updateEntity(dto, user);

        user.setName(sanitize(user.getName(), "name"));
        user.setCep(sanitize(user.getCep(), "cep"));
    }
}
