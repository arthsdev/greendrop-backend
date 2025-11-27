package br.com.greendrop.backend.dto.auth;

import br.com.greendrop.backend.dto.user.UserResponseDTO;

/**
 * Internal package returned by AuthService containing both tokens and user info.
 * Note: refreshToken must NOT be returned to client body; controller writes it to cookie.
 */
public record AuthTokens(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UserResponseDTO user
) {}

