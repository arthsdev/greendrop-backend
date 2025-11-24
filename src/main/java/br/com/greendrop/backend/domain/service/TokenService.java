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

    // Access token TTL must match JWT configuration
    private static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);

    // Refresh token TTL typically ranges from 7 to 30 days
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    // =============================================================
    // SAVE ACCESS + REFRESH TOKENS
    // =============================================================
    public void saveTokens(UUID userId, String accessToken, String refreshToken) {
        Instant now = Instant.now();

        // Build access token entity
        Token access = Token.builder()
                .userId(userId)
                .value(accessToken)
                .type("ACCESS")
                .createdAt(now)
                .expiresAt(now.plus(ACCESS_TOKEN_TTL))
                .build();

        // Build refresh token entity
        Token refresh = Token.builder()
                .userId(userId)
                .value(refreshToken)
                .type("REFRESH")
                .createdAt(now)
                .expiresAt(now.plus(REFRESH_TOKEN_TTL))
                .build();

        // Save tokens into Redis with TTL
        redisTokenRepository.saveAccessToken(access, ACCESS_TOKEN_TTL);
        redisTokenRepository.saveRefreshToken(refresh, REFRESH_TOKEN_TTL);
    }

    // =============================================================
    // REVOKE ONLY THE REFRESH TOKEN
    // =============================================================
    public void revokeToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;

        // Deletes the refresh token from Redis (invalidates login session)
        redisTokenRepository.deleteRefreshToken(refreshToken);
    }

    // =============================================================
    // DELETE ALL TOKENS FOR A USER (LOGOUT EVERYWHERE)
    // =============================================================
    public void deleteAllUserTokens(String userId) {
        if (userId == null) return;

        // Removes all access + refresh tokens for the given user
        redisTokenRepository.deleteAllUserTokens(userId);
    }

    // =============================================================
    // VALIDATE REFRESH TOKEN (not expired + not revoked/blacklisted)
    // =============================================================
    public boolean isTokenValid(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            return false;
        }

        // Token explicitly blacklisted = invalid
        if (redisTokenRepository.isBlacklisted(refreshToken)) {
            return false;
        }

        // Fetch from Redis
        Optional<Token> stored = redisTokenRepository.findRefreshToken(refreshToken);

        // Token must exist AND be unexpired
        return stored.isPresent() && !stored.get().isExpired();
    }

    // =============================================================
    // BLACKLIST ANY TOKEN (ACCESS OR REFRESH)
    // =============================================================
    public void blacklistToken(String tokenValue) {
        if (tokenValue == null || tokenValue.isBlank()) return;

        // If exists as refresh token, blacklist it
        redisTokenRepository.findRefreshToken(tokenValue)
                .ifPresent(redisTokenRepository::blacklistToken);

        // If exists as access token, blacklist it
        redisTokenRepository.findAccessToken(tokenValue)
                .ifPresent(redisTokenRepository::blacklistToken);
    }
}
