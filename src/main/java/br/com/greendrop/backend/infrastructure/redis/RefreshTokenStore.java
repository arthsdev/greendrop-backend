package br.com.greendrop.backend.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Key-value store for refresh token references in Redis.
 *
 * Key pattern: "refresh:{sha256(jti)}" → value = userId (string)
 * TTL: Matches the refresh token expiration time.
 */

@Repository
@RequiredArgsConstructor
public class RefreshTokenStore {

    private static final String KEY_PREFIX = "refresh:";
    private final StringRedisTemplate redis;

    public void saveByJtiHash(String jtiHash, String userId, Duration ttl) {
        if (jtiHash == null || jtiHash.isBlank()) return;
        redis.opsForValue().set(KEY_PREFIX + jtiHash, userId, ttl.getSeconds(), TimeUnit.SECONDS);
    }

    public boolean existsByJtiHash(String jtiHash) {
        if (jtiHash == null || jtiHash.isBlank()) return false;
        return Boolean.TRUE.equals(redis.hasKey(KEY_PREFIX + jtiHash));
    }

    public void deleteByJtiHash(String jtiHash) {
        if (jtiHash == null || jtiHash.isBlank()) return;
        redis.delete(KEY_PREFIX + jtiHash);
    }

    public String getUserIdByJtiHash(String jtiHash) {
        if (jtiHash == null || jtiHash.isBlank()) return null;
        return redis.opsForValue().get(KEY_PREFIX + jtiHash);
    }
}
