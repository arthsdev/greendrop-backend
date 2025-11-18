package br.com.greendrop.backend.infrastructure.redis;

import br.com.greendrop.backend.domain.model.Token;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Repository responsible for managing access, refresh, and blacklisted tokens in Redis.
 * Uses Token objects serialized as JSON for flexibility and maintainability.
 */
@Repository
@RequiredArgsConstructor
public class RedisTokenRepository {

    private static final String PREFIX_ACCESS = "token:access:";
    private static final String PREFIX_REFRESH = "token:refresh:";
    private static final String PREFIX_BLACKLIST = "token:blacklist:";

    private final RedisTemplate<String, Object> redisTemplate;

    // -----------------------------
    // Access Token operations
    // -----------------------------

    public void saveAccessToken(Token token, Duration ttl) {
        String key = PREFIX_ACCESS + token.getValue();
        redisTemplate.opsForValue().set(key, token, ttl.getSeconds(), TimeUnit.SECONDS);
    }

    public Optional<Token> findAccessToken(String tokenValue) {
        Object stored = redisTemplate.opsForValue().get(PREFIX_ACCESS + tokenValue);
        return Optional.ofNullable(stored).map(o -> (Token) o);
    }

    public void deleteAccessToken(String tokenValue) {
        redisTemplate.delete(PREFIX_ACCESS + tokenValue);
    }

    // -----------------------------
    // Refresh Token operations
    // -----------------------------

    public void saveRefreshToken(Token token, Duration ttl) {
        String key = PREFIX_REFRESH + token.getValue();
        redisTemplate.opsForValue().set(key, token, ttl.getSeconds(), TimeUnit.SECONDS);
    }

    public Optional<Token> findRefreshToken(String tokenValue) {
        Object stored = redisTemplate.opsForValue().get(PREFIX_REFRESH + tokenValue);
        return Optional.ofNullable(stored).map(o -> (Token) o);
    }

    public void deleteRefreshToken(String tokenValue) {
        redisTemplate.delete(PREFIX_REFRESH + tokenValue);
    }

    // -----------------------------
    // Blacklist operations
    // -----------------------------

    /**
     * Adds a token to the blacklist for the remaining lifetime.
     */
    public void blacklistToken(Token token) {
        String key = PREFIX_BLACKLIST + token.getValue();
        long ttl = token.getRemainingTTLSeconds();
        redisTemplate.opsForValue().set(key, true, ttl, TimeUnit.SECONDS);
    }

    /**
     * Checks if a token is blacklisted.
     */
    public boolean isBlacklisted(String tokenValue) {
        Boolean exists = redisTemplate.hasKey(PREFIX_BLACKLIST + tokenValue);
        return Boolean.TRUE.equals(exists);
    }

    // -----------------------------
    //  Utility
    // -----------------------------

    /**
     * Deletes all tokens related to a specific user (access + refresh).
     * This is optional and not O(1), but useful for forced logout or security resets.
     */
    public void deleteAllUserTokens(String userId) {
        redisTemplate.delete(redisTemplate.keys(PREFIX_ACCESS + "user:" + userId + "*"));
        redisTemplate.delete(redisTemplate.keys(PREFIX_REFRESH + "user:" + userId + "*"));
    }
}
