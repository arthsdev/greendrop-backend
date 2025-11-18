package br.com.greendrop.backend.dto.auth;

import br.com.greendrop.backend.dto.user.UserResponseDTO;

/**
 * Response returned to the client after authentication or registration.
 *
 * Includes:
 *  - JWT access token (short-lived)
 *  - User data (non-sensitive)
 *
 * Note: Refresh token is managed by Redis and NOT returned in the body.
 */
public record AuthResponseDTO(
        String accessToken,
        UserResponseDTO user
) {}
