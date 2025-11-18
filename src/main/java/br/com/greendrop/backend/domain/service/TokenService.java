package br.com.greendrop.backend.domain.service;

import br.com.greendrop.backend.domain.model.Token;
import br.com.greendrop.backend.infrastructure.redis.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages token lifecycle (save, revoke, validate) and integrates with Redis storage.
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    private final RedisTokenRepository redisTokenRepository;

    // Access tokens typically expire quickly (e.g., 15 min)
    private static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);

    // Refresh tokens typically expire in days (e.g., 7 days)
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    /**
     * Saves access and refresh tokens to Redis with appropriate TTL.
     */
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

    /**
     * Revokes a single refresh token (logout).
     */
    public void revokeToken(String refreshToken) {
        redisTokenRepository.deleteRefreshToken(refreshToken);
    }

    /**
     * Revokes all tokens associated with a user (e.g., when user logs in again).
     */
    public void deleteAllUserTokens(String userId) {
        redisTokenRepository.deleteAllUserTokens(userId);
    }

    /**
     * Checks if a refresh token exists and is not expired or blacklisted.
     */
    public boolean isTokenValid(String refreshToken) {
        if (redisTokenRepository.isBlacklisted(refreshToken)) return false;

        Optional<Token> stored = redisTokenRepository.findRefreshToken(refreshToken);
        return stored.isPresent() && !stored.get().isExpired();
    }

    /**
     * Adds a token to the blacklist to prevent reuse after logout.
     */
    public void blacklistToken(String tokenValue) {
        redisTokenRepository.findAccessToken(tokenValue)
                .ifPresent(redisTokenRepository::blacklistToken);
    }
}
