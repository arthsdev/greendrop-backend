package br.com.greendrop.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pair of access and refresh tokens, without user data.")
public record TokenPairDTO(

        @Schema(description = "JWT access token.")
        String accessToken,

        @Schema(description = "JWT refresh token.")
        String refreshToken
) {}
