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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UserRateLimitService userRateLimitService;
    private final UserCacheService userCacheService;

    // Time-to-live of the access token
    private static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);

    // ============================================================
    // REGISTER
    // ============================================================
    @Transactional
    public AuthTokens register(UserRequestDTO request) {
        // Validate email uniqueness and password strength
        validateEmailNotUsed(request.email());
        validatePasswordStrength(request.password());

        // Build user entity and save
        User user = createUserFromRequest(request);
        userRepository.save(user);

        // Issue tokens for the new user
        return issueAuthTokens(user);
    }

    // ============================================================
    // LOGIN
    // ============================================================
    @Transactional
    public AuthTokens login(String email, String password) {
        User user = findActiveUserByEmail(email);

        // Rate limit check
        validateRateLimit(user);

        // Validate password
        validatePassword(password, user.getPassword());

        // Clear failed attempts cache
        resetLoginAttempts(user.getId().toString());

        // Revoke all previous tokens for this user
        tokenService.deleteAllUserTokens(user.getId().toString());

        // Issue new tokens
        return issueAuthTokens(user);
    }

    // ============================================================
    // REFRESH TOKEN
    // ============================================================
    @Transactional
    public AuthTokens refresh(String oldRefreshToken) {
        validateRefreshTokenExists(oldRefreshToken);
        validateRefreshTokenIntegrity(oldRefreshToken);

        UUID userId = extractUserId(oldRefreshToken);
        User user = findActiveUserById(userId);

        // Revoke old refresh token
        tokenService.revokeToken(oldRefreshToken);
        tokenService.blacklistToken(oldRefreshToken);

        // Return new access + refresh token
        return issueAuthTokens(user);
    }

    // ============================================================
    // LOGOUT
    // ============================================================
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;

        tokenService.revokeToken(refreshToken);
        tokenService.blacklistToken(refreshToken);
    }

    // ============================================================
    // PRIVATE HELPERS
    // ============================================================

    private void validateEmailNotUsed(String email) {
        if (userRepository.existsByEmail(email)) throw new DuplicateResourceException();
    }

    private void validateRateLimit(User user) {
        userRateLimitService.recordLoginAttempt(user.getId().toString());
    }

    private User findActiveUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        if (!user.isActive()) throw new UserInactiveException();
        return user;
    }

    private User findActiveUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
        if (!user.isActive()) throw new UserInactiveException();
        return user;
    }

    private void resetLoginAttempts(String userId) {
        userCacheService.clearLoginAttempts(userId);
    }

    private void validatePassword(String raw, String encoded) {
        if (!passwordEncoder.matches(raw, encoded)) throw new InvalidCredentialsException();
    }

    private void validateRefreshTokenExists(String token) {
        if (token == null || token.isBlank()) throw new MissingTokenException();
    }

    private void validateRefreshTokenIntegrity(String token) {
        if (!jwtService.validateToken(token) || !tokenService.isTokenValid(token)) {
            throw new InvalidTokenException();
        }
    }

    private UUID extractUserId(String token) {
        return UUID.fromString(jwtService.extractUserId(token));
    }

    private User createUserFromRequest(UserRequestDTO request) {
        return User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .cep(request.cep())
                .latitude(request.latitude())
                .longitude(request.longitude())
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

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) throw new InvalidCredentialsException();
    }

    // ============================================================
    // TOKEN FACTORY
    // ============================================================
    private AuthTokens issueAuthTokens(User user) {
        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);
        tokenService.saveTokens(user.getId(), access, refresh);

        long expiresInSeconds = ACCESS_TOKEN_TTL.toSeconds();

        // Return both tokens, expiration, and user info
        return new AuthTokens(
                access,
                refresh,
                expiresInSeconds,
                buildUserResponse(user)
        );
    }
}
