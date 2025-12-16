package br.com.greendrop.backend.domain.service;

import br.com.greendrop.backend.cache.user.UserCacheService;
import br.com.greendrop.backend.cache.user.UserRateLimitService;
import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.domain.model.enums.Role;
import br.com.greendrop.backend.domain.repository.UserRepository;
import br.com.greendrop.backend.dto.auth.AuthTokens;
import br.com.greendrop.backend.dto.user.UserRequestDTO;
import br.com.greendrop.backend.dto.user.UserResponseDTO;
import br.com.greendrop.backend.exception.auth.*;
import br.com.greendrop.backend.exception.user.DuplicateResourceException;
import br.com.greendrop.backend.exception.user.ResourceNotFoundException;
import br.com.greendrop.backend.infrastructure.security.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * AuthService
 *
 * Provides end-to-end authentication flows:
 * ▸ register new user
 * ▸ login
 * ▸ refresh token (using JTI rotation)
 * ▸ logout (revokes current refresh token)
 *
 * This service coordinates:
 * ▸ JWT generation (JwtService)
 * ▸ refresh token hashing & rotation (TokenService)
 * ▸ rate limiting (UserRateLimitService)
 * ▸ login attempt caching (UserCacheService)
 *
 * The service does NOT store sessions in DB.
 * Only JTI hashes are stored in Redis (via TokenService).
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UserRateLimitService userRateLimitService;
    private final UserCacheService userCacheService;
    private final HttpServletRequest request;

    // =========================================================================
    // REGISTER
    // =========================================================================

    /**
     * Registers a new user in the system.
     * Flow:
     * 1) Validate that the email is not already used
     * 2) Validate the password strength
     * 3) Create a User entity from the request DTO
     * 4) Persist the new User in the database
     * 5) Return sanitized user data (UserResponseDTO)
     */
    @Transactional
    public UserResponseDTO register(UserRequestDTO requestDto) {
        validateEmailNotUsed(requestDto.email());
        validatePasswordStrength(requestDto.password());

        User user = createUserFromRequest(requestDto);
        userRepository.save(user);

        return buildUserResponse(user);
    }

    // =========================================================================
    // LOGIN
    // =========================================================================

    /**
     * Logs a user into the system.
     *
     * Flow:
     * 1) find user by e-mail
     * 2) rate-limit protection (increment attempt counter)
     * 3) password check
     * 4) reset login attempts
     * 5) issue new tokens
     */
    @Transactional
    public AuthTokens login(String email, String password) {
        User user = findActiveUserByEmail(email);

        validateRateLimit(user);
        validatePassword(password, user.getPassword());
        resetLoginAttempts(user.getId().toString());

        // Optional: logout all devices
        // tokenService.deleteAllUserRefreshTokens(user.getId().toString());

        return issueNewSessionTokens(user);
    }

    // =========================================================================
    // REFRESH TOKEN
    // =========================================================================

    /**
     * Refreshes authentication using JTI rotation.
     *
     * Steps:
     * 1) check non-null token
     * 2) ensure type = "refresh"
     * 3) structural JWT validation
     * 4) ensure the JTI hash exists in Redis
     * 5) load user from DB
     * 6) rotate: delete old JTI → generate new JTI → save hash → create new token
     * 7) issue new access token
     */
    @Transactional
    public AuthTokens refresh(String oldRefreshToken) {

        if (oldRefreshToken == null || oldRefreshToken.isBlank())
            throw new MissingTokenException();

        // Must explicitly be a refresh token
        if (!"refresh".equals(jwtService.extractType(oldRefreshToken)))
            throw new InvalidTokenException();

        // Structural validation (signature, expiration)
        if (!jwtService.validateToken(oldRefreshToken))
            throw new InvalidTokenException();

        // JTI validation (hash stored in Redis)
        if (!tokenService.isRefreshTokenValid(oldRefreshToken))
            throw new InvalidTokenException();

        UUID userId = UUID.fromString(jwtService.extractUserId(oldRefreshToken));
        User user = findActiveUserById(userId);

        // Rotate refresh token
        String newRefresh = tokenService.rotateRefreshToken(oldRefreshToken, user);

        // Create new access token
        String newAccess = jwtService.generateAccessToken(user);

        return new AuthTokens(newAccess, newRefresh,
                jwtService.getAccessTokenExpirationSeconds(),
                buildUserResponse(user));
    }

    // =========================================================================
    // LOGOUT
    // =========================================================================

    /**
     * Logs out the user by revoking the refresh token.
     */
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;
        tokenService.revokeRefreshToken(refreshToken);
    }

    // =========================================================================
    // PRIVATE VALIDATION HELPERS
    // =========================================================================

    private void validateEmailNotUsed(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException();
        }
    }

    private void validateRateLimit(User user) {
        // Will throw if too many attempts
        userRateLimitService.recordLoginAttempt(user.getId().toString());
    }

    private User findActiveUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive())
            throw new UserInactiveException();

        return user;
    }

    private User findActiveUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);

        if (!user.isActive())
            throw new UserInactiveException();

        return user;
    }

    private void resetLoginAttempts(String userId) {
        userCacheService.clearLoginAttempts(userId);
    }

    private void validatePassword(String raw, String encoded) {
        if (!passwordEncoder.matches(raw, encoded))
            throw new InvalidCredentialsException();
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8)
            throw new InvalidCredentialsException();
    }

    // =========================================================================
    // TOKEN ISSUANCE
    // =========================================================================

    /**
     * Creates a new authentication session:
     * ▸ generates access token
     * ▸ generates refresh token + persists its JTI hash
     */
    private AuthTokens issueNewSessionTokens(User user) {

        String access = jwtService.generateAccessToken(user);
        String refresh = tokenService.createAndStoreRefreshToken(user);

        // Optional metadata tracking (for auditing, sessions, logs)
        String userAgent = safeHeader("User-Agent");
        String deviceName = safeHeader("X-Device-Name");
        if (deviceName == null || deviceName.isBlank()) deviceName = userAgent;

        String ip = extractClientIp();

        // NOTE: if you want DB session tracking, build it here.

        return new AuthTokens(
                access,
                refresh,
                jwtService.getAccessTokenExpirationSeconds(),
                buildUserResponse(user)
        );
    }

    private String safeHeader(String name) {
        try {
            String v = request.getHeader(name);
            return (v == null || v.isBlank()) ? null : v;
        } catch (Exception e) {
            return null;
        }
    }

    private String extractClientIp() {
        try {
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank())
                return xff.split(",")[0].trim();
            return request.getRemoteAddr();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private User createUserFromRequest(UserRequestDTO dto) {
        return User.builder()
                .name(dto.name())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .role(Role.USER) // default role at registration
                .cep(dto.cep())
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .active(true)
                .build();
    }

    private UserResponseDTO buildUserResponse(User user) {
        return new UserResponseDTO(
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCep(),
                user.getLatitude(),
                user.getLongitude(),
                user.getPoints()
        );
    }
}