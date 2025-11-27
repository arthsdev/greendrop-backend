package br.com.greendrop.backend.cache.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class UserCacheService {

    private final StringRedisTemplate redisTemplate;

    private static final String LOGIN_ATTEMPTS_KEY = "login_attempts:";
    private static final Duration LOGIN_ATTEMPT_TTL = Duration.ofMinutes(5);

    public UserCacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Increments login attempts for a given user.
     * Ensures TTL exists on first increment or if key had no TTL.
     */
    public Long increaseLoginAttempts(String userId) {
        String key = LOGIN_ATTEMPTS_KEY + userId;

        Long attempts = redisTemplate.opsForValue().increment(key);

        // Fallback in case Redis returns null (rare network/cluster issue)
        if (attempts == null) {
            log.warn("Redis increment returned null for key {}. Using safe fallback.", key);
            attempts = 1L;
            redisTemplate.opsForValue().set(key, "1", LOGIN_ATTEMPT_TTL);
            return attempts;
        }

        ensureTTL(key, attempts);
        return attempts;
    }

    /**
     * Applies TTL if this is the first attempt or if Redis key has no expiration.
     */
    private void ensureTTL(String key, Long attempts) {
        // First increment → always set TTL
        if (attempts == 1L) {
            redisTemplate.expire(key, LOGIN_ATTEMPT_TTL);
            return;
        }

        // TTL -1 means TTL is missing → enforce it
        Long ttl = redisTemplate.getExpire(key);

        if (ttl == -1) { // key exists but has no TTL
            redisTemplate.expire(key, LOGIN_ATTEMPT_TTL);
        }
    }

    /**
     * Retrieves login attempt count.
     * Returns 0 if key does not exist or value is corrupted.
     */
    public Long getLoginAttempts(String userId) {
        String key = LOGIN_ATTEMPTS_KEY + userId;
        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return 0L;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            log.error("Corrupted Redis value for key {}. Resetting and returning 0.", key, ex);
            redisTemplate.delete(key);
            return 0L;
        }
    }

    /**
     * Clears the stored login attempt counter.
     */
    public void clearLoginAttempts(String userId) {
        String key = LOGIN_ATTEMPTS_KEY + userId;
        redisTemplate.delete(key);
    }
}
