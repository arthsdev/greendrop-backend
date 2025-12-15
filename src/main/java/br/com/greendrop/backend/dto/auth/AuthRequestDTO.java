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
        @NotBlank(message = "{error.email.required}")
        @Email(message = "{error.email.invalid}")
        String email,

        @Schema(description = "User password.", example = "StrongPass123!")
        @NotBlank(message = "{error.password.required}")
        String password
) {}