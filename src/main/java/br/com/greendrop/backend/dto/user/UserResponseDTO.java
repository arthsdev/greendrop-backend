package br.com.greendrop.backend.dto.user;

// DTO for returning user information in API responses
public record UserResponseDTO(
        Long id,
        String name,
        String email,
        String role,       // USER or COLLECTOR
        String cep,
        Double latitude,
        Double longitude,
        Integer points     // gamification points
) {}
