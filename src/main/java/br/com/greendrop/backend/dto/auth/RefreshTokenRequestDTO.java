package br.com.greendrop.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request used if client sends refresh token in body.
 */
public record RefreshTokenRequestDTO(@NotBlank String refreshToken) {}
