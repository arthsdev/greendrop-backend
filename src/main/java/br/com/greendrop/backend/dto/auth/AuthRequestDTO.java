package br.com.greendrop.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload used by clients to authenticate (login).
 */
@Schema(description = "Login request payload.")
public record AuthRequestDTO(

        @Schema(description = "User email.", example = "fabiano@example.com")
        @NotBlank @Email
        String email,

        @Schema(description = "User password.", example = "StrongPass123!")
        @NotBlank
        String password
) {}
