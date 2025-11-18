package br.com.greendrop.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Login request.
 */
public record AuthRequestDTO(
        @NotBlank @Email String email,
        @NotBlank String password
) {}
