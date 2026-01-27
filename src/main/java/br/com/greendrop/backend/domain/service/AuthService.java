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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final UserRateLimitService userRateLimitService;
    private final UserCacheService userCacheService;
    private final PasswordEncoder passwordEncoder;
    private final HttpServletRequest request;

    // =========================================================================
    // REGISTER
    // =========================================================================

    @Transactional
    public UserResponseDTO register(UserRequestDTO dto) {
        validateEmailNotUsed(dto.email());
        validatePasswordStrength(dto.password());

        User user = User.builder()
                .name(dto.name())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .role(Role.USER)
                .cep(dto.cep())
                .active(true)
                .build();

        userRepository.save(user);
        return buildUserResponse(user);
    }

    // =========================================================================
    // LOGIN
    // =========================================================================

    /**
     * Login flow:
     * 1) rate-limit validation
     * 2) authenticate credentials (Spring Security)
     * 3) reset login attempts
     * 4) issue access + refresh tokens
     */
    @Transactional
    public AuthTokens login(String email, String password) {

        // Step 1 — rate limit by email (pre-auth)
        userRateLimitService.recordLoginAttempt(email);

        // Step 2 — authenticate
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        User user = (User) authentication.getPrincipal();

        // Step 3 — post-auth success handling
        handleSuccessfulLogin(user);

        // Step 4 — issue tokens
        return issueNewSessionTokens(user);
    }

    private void handleSuccessfulLogin(User user) {
        userCacheService.clearLoginAttempts(user.getId().toString());
    }

    // =========================================================================
    // REFRESH TOKEN
    // =========================================================================

    @Transactional
    public AuthTokens refresh(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank())
            throw new MissingTokenException();

        if (!"refresh".equals(jwtService.extractType(refreshToken)))
            throw new InvalidTokenException();

        if (!jwtService.validateToken(refreshToken))
            throw new InvalidTokenException();

        if (!tokenService.isRefreshTokenValid(refreshToken))
            throw new InvalidTokenException();

        UUID userId = UUID.fromString(jwtService.extractUserId(refreshToken));
        User user = findActiveUserById(userId);

        String newRefresh = tokenService.rotateRefreshToken(refreshToken, user);
        String newAccess = jwtService.generateAccessToken(user);

        return new AuthTokens(
                newAccess,
                newRefresh,
                jwtService.getAccessTokenExpirationSeconds(),
                buildUserResponse(user)
        );
    }

    // =========================================================================
    // LOGOUT
    // =========================================================================

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            tokenService.revokeRefreshToken(refreshToken);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private void validateEmailNotUsed(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException();
        }
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new InvalidCredentialsException();
        }
    }

    private User findActiveUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);

        if (!user.isActive())
            throw new UserInactiveException();

        return user;
    }

    private AuthTokens issueNewSessionTokens(User user) {

        String access = jwtService.generateAccessToken(user);
        String refresh = tokenService.createAndStoreRefreshToken(user);

        // Optional metadata (future-proof)
        String ip = extractClientIp();
        String device = safeHeader("User-Agent");

        return new AuthTokens(
                access,
                refresh,
                jwtService.getAccessTokenExpirationSeconds(),
                buildUserResponse(user)
        );
    }

    private String safeHeader(String name) {
        try {
            String value = request.getHeader(name);
            return (value == null || value.isBlank()) ? null : value;
        } catch (Exception e) {
            return null;
        }
    }

    private String extractClientIp() {
        try {
            String xff = request.getHeader("X-Forwarded-For");
            return (xff != null && !xff.isBlank())
                    ? xff.split(",")[0].trim()
                    : request.getRemoteAddr();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private UserResponseDTO buildUserResponse(User user) {
        return new UserResponseDTO(
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCep(),
                user.getPoints()
        );
    }
}