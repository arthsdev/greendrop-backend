package br.com.greendrop.backend.dto.user;

import br.com.greendrop.backend.domain.model.enums.Role;

/**
 * DTO returned in responses (e.g., after login or registration).
 * Represents a sanitized version of the User entity.
 */
public record UserResponseDTO(
        String id,
        String name,
        String email,
        Role role,
        String cep,
        Double latitude,
        Double longitude,
        Integer points
) {}
