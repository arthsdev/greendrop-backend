package br.com.greendrop.backend.mapper.user;

import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.dto.user.UserRequestDTO;
import br.com.greendrop.backend.dto.user.UserResponseDTO;
import br.com.greendrop.backend.dto.user.UserUpdateDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Decorator responsible for sanitizing user input:
 * - trims strings
 * - normalizes whitespace
 * - converts email to lowercase
 *
 * No business logic should live here.
 */
@Slf4j
public abstract class UserMapperDecorator implements UserMapper {

    protected UserMapper delegate;

    @Autowired
    public void setDelegate(UserMapper delegate) {
        this.delegate = delegate;
    }

    private String sanitize(String value, String fieldName) {
        if (value == null) return null;
        String sanitized = value.trim().replaceAll("\\s+", " ");
        if (!value.equals(sanitized)) {
            log.debug("[UserMapper] Sanitized '{}' from '{}' to '{}'", fieldName, value, sanitized);
        }
        return sanitized;
    }

    @Override
    public UserResponseDTO toResponse(User user) {
        // N+1 safe because user is already fully loaded by service
        return delegate.toResponse(user);
    }

    @Override
    public User toEntity(UserRequestDTO dto) {
        User user = delegate.toEntity(dto);
        user.setName(sanitize(user.getName(), "name"));
        user.setCep(sanitize(user.getCep(), "cep"));
        if (user.getEmail() != null) {
            user.setEmail(sanitize(user.getEmail(), "email").toLowerCase());
        }
        return user;
    }

    @Override
    public void updateEntity(UserUpdateDTO dto, User user) {
        delegate.updateEntity(dto, user);
        user.setName(sanitize(user.getName(), "name"));
        user.setCep(sanitize(user.getCep(), "cep"));
        if (user.getEmail() != null) {
            user.setEmail(sanitize(user.getEmail(), "email").toLowerCase());
        }
    }
}
