package br.com.greendrop.backend.infrastructure.redis;

import br.com.greendrop.backend.domain.model.Token;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis repository for access, refresh, and blacklisted tokens.
 * Fully type-safe with Token objects.
 */
@Repository
@RequiredArgsConstructor
public class RedisTokenRepository {

    private static final String PREFIX_ACCESS = "token:access:";
    private static final String PREFIX_REFRESH = "token:refresh:";
    private static final String PREFIX_BLACKLIST = "token:blacklist:";
    private static final String PREFIX_USER_TOKENS = "user:tokens:";

    private final RedisTemplate<String, Token> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate; // For sets of token IDs per user

    // ==============================
    // Generic token operations
    // ==============================
    private void saveToken(String prefix, Token token, Duration ttl) {
        if (token == null || token.getValue() == null || token.getValue().isBlank()) return;
        redisTemplate.opsForValue().set(prefix + token.getValue(), token, ttl.getSeconds(), TimeUnit.SECONDS);

        // Track token in user's set
        stringRedisTemplate.opsForSet().add(PREFIX_USER_TOKENS + token.getUserId(), token.getValue());
    }

    private Optional<Token> findToken(String prefix, String tokenValue) {
        if (tokenValue == null || tokenValue.isBlank()) return Optional.empty();
        return Optional.ofNullable(redisTemplate.opsForValue().get(prefix + tokenValue));
    }

    private void deleteToken(String prefix, String tokenValue) {
        if (tokenValue == null || tokenValue.isBlank()) return;
        redisTemplate.delete(prefix + tokenValue);
    }

    // ==============================
    // Access Token operations
    // ==============================
    public void saveAccessToken(Token token, Duration ttl) {
        saveToken(PREFIX_ACCESS, token, ttl);
    }

    public Optional<Token> findAccessToken(String tokenValue) {
        return findToken(PREFIX_ACCESS, tokenValue);
    }

    public void deleteAccessToken(String tokenValue) {
        deleteToken(PREFIX_ACCESS, tokenValue);
    }

    // ==============================
    // Refresh Token operations
    // ==============================
    public void saveRefreshToken(Token token, Duration ttl) {
        saveToken(PREFIX_REFRESH, token, ttl);
    }

    public Optional<Token> findRefreshToken(String tokenValue) {
        return findToken(PREFIX_REFRESH, tokenValue);
    }

    public void deleteRefreshToken(String tokenValue) {
        deleteToken(PREFIX_REFRESH, tokenValue);
    }

    // ==============================
    // Blacklist operations
    // ==============================
    public void blacklistToken(Token token) {
        if (token == null || token.getValue() == null || token.getValue().isBlank()) return;
        long ttl = token.getRemainingTTLSeconds();
        if (ttl > 0) {
            redisTemplate.opsForValue().set(PREFIX_BLACKLIST + token.getValue(), token, ttl, TimeUnit.SECONDS);
        }
    }

    public boolean isBlacklisted(String tokenValue) {
        if (tokenValue == null || tokenValue.isBlank()) return true;
        return redisTemplate.hasKey(PREFIX_BLACKLIST + tokenValue);
    }

    public void deleteBlacklistToken(String tokenValue) {
        deleteToken(PREFIX_BLACKLIST, tokenValue);
    }

    // ==============================
    // Delete all tokens for a user
    // ==============================
    public void deleteAllUserTokens(String userId) {
        if (userId == null || userId.isBlank()) return;

        String userSetKey = PREFIX_USER_TOKENS + userId;
        Set<String> tokens = stringRedisTemplate.opsForSet().members(userSetKey);

        if (tokens != null) {
            for (String tokenValue : tokens) {
                deleteAccessToken(tokenValue);
                deleteRefreshToken(tokenValue);
                deleteBlacklistToken(tokenValue);
            }
        }

        stringRedisTemplate.delete(userSetKey);
    }
}
