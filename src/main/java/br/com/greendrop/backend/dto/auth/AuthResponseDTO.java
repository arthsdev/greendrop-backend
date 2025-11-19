package br.com.greendrop.backend.dto.auth;

import br.com.greendrop.backend.dto.user.UserResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response returned after successful authentication or registration.
 * Contains the access token and sanitized user data.
 */
@Schema(description = "Response returned after authentication.")
public record AuthResponseDTO(

        @Schema(description = "JWT access token.", example = "eyJhbGciOiJIUzI1NiIs...")
        String accessToken,

        @Schema(description = "Authenticated user data.")
        UserResponseDTO user
) {}
