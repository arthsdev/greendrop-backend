package br.com.greendrop.backend.cache.user;

import br.com.greendrop.backend.exception.user.TooManyLoginAttemptsException;
import org.springframework.stereotype.Service;

@Service
public class UserRateLimitService {

    private final UserCacheService userCacheService;

    // Maximum number of login attempts allowed within TTL window
    private static final int MAX_ATTEMPTS = 5;

    public UserRateLimitService(UserCacheService userCacheService) {
        this.userCacheService = userCacheService;
    }

    /**
     * Validates whether the user has exceeded the login attempt limit.
     */
    public void validateLoginLimit(String userId) {
        Long attempts = userCacheService.getLoginAttempts(userId);
        enforceLimit(attempts);
    }

    /**
     * Records a new login attempt and validates the rate limit.
     *
     * @return updated number of attempts
     */
    public Long recordLoginAttempt(String userId) {
        Long attempts = userCacheService.increaseLoginAttempts(userId);
        enforceLimit(attempts);
        return attempts;
    }

    /**
     * Clears the user's login attempt counter after successful authentication.
     */
    public void clearLoginAttempts(String userId) {
        userCacheService.clearLoginAttempts(userId);
    }

    /**
     * Centralized check to avoid duplicated logic.
     * Throws business exception if user reached the max attempts.
     */
    private void enforceLimit(Long attempts) {
        if (attempts >= MAX_ATTEMPTS) {
            throw new TooManyLoginAttemptsException();
        }
    }
}
