package br.com.greendrop.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload used when the client requests a new access token using a refresh token.
 */
@Schema(description = "Refresh token request payload.")
public record RefreshTokenRequestDTO(

        @Schema(description = "Refresh token string.", example = "a9f8d1c0...")
        @NotBlank
        String refreshToken
) {}
