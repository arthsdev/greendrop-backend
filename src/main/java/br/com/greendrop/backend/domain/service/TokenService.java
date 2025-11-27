package br.com.greendrop.backend.domain.service;

import br.com.greendrop.backend.domain.model.Token;
import br.com.greendrop.backend.infrastructure.redis.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RedisTokenRepository redisTokenRepository;

    private static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    // -------------------------
    // Save tokens
    // -------------------------
    public void saveTokens(UUID userId, String accessToken, String refreshToken) {
        Instant now = Instant.now();

        Token access = Token.builder()
                .userId(userId)
                .value(accessToken)
                .type("ACCESS")
                .createdAt(now)
                .expiresAt(now.plus(ACCESS_TOKEN_TTL))
                .build();

        Token refresh = Token.builder()
                .userId(userId)
                .value(refreshToken)
                .type("REFRESH")
                .createdAt(now)
                .expiresAt(now.plus(REFRESH_TOKEN_TTL))
                .build();

        redisTokenRepository.saveAccessToken(access, ACCESS_TOKEN_TTL);
        redisTokenRepository.saveRefreshToken(refresh, REFRESH_TOKEN_TTL);
    }

    // -------------------------
    // Revoke refresh token only
    // -------------------------
    public void revokeToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;

        // Delete refresh token
        redisTokenRepository.deleteRefreshToken(refreshToken);

        // Remove from blacklist (if exists)
        redisTokenRepository.deleteBlacklistToken(refreshToken);
    }

    // -------------------------
    // Delete all tokens for a user
    // -------------------------
    public void deleteAllUserTokens(String userId) {
        redisTokenRepository.deleteAllUserTokens(userId);
    }

    // -------------------------
    // Validate refresh token
    // -------------------------
    public boolean isTokenValid(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return false;

        // Check blacklist first
        if (redisTokenRepository.isBlacklisted(refreshToken)) return false;

        Optional<Token> tokenOpt = redisTokenRepository.findRefreshToken(refreshToken);
        return tokenOpt.map(token -> !token.isExpired()).orElse(false);
    }

    // -------------------------
    // Blacklist token (access or refresh)
    // -------------------------
    public void blacklistToken(String tokenValue) {
        if (tokenValue == null || tokenValue.isBlank()) return;

        redisTokenRepository.findRefreshToken(tokenValue)
                .ifPresent(redisTokenRepository::blacklistToken);

        redisTokenRepository.findAccessToken(tokenValue)
                .ifPresent(redisTokenRepository::blacklistToken);
    }

    // -------------------------
    // Check if token is revoked
    // -------------------------
    public boolean isTokenRevoked(String tokenValue) {
        if (tokenValue == null || tokenValue.isBlank()) return true;
        return redisTokenRepository.isBlacklisted(tokenValue);
    }
}
