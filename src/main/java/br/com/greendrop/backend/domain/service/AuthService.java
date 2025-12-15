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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

/**
 * Authentication service handling register, login, refresh and logout flows.
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

    // Access token duration
    private static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);

    // ============================================================
    // REGISTER
    // ============================================================
    @Transactional
    public AuthTokens register(UserRequestDTO request) {

        validateEmailNotUsed(request.email());
        validatePasswordStrength(request.password());

        User user = createUserFromRequest(request);
        userRepository.save(user);

        return issueAuthTokens(user);
    }

    // ============================================================
    // LOGIN
    // ============================================================
    @Transactional
    public AuthTokens login(String email, String password) {
        User user = findActiveUserByEmail(email);

        validateRateLimit(user);
        validatePassword(password, user.getPassword());

        resetLoginAttempts(user.getId().toString());

        tokenService.deleteAllUserTokens(user.getId().toString());

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

        tokenService.revokeToken(oldRefreshToken);
        tokenService.blacklistToken(oldRefreshToken);

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
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException();
        }
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
        if (!passwordEncoder.matches(raw, encoded)) {
            throw new InvalidCredentialsException();
        }
    }

    private void validateRefreshTokenExists(String token) {
        if (token == null || token.isBlank()) {
            throw new MissingTokenException();
        }
    }

    private void validateRefreshTokenIntegrity(String token) {

        if (!"refresh".equals(jwtService.extractType(token))) {
            throw new InvalidTokenException();
        }

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
        if (password == null || password.length() < 8) {
            throw new InvalidCredentialsException();
        }
    }

    // ============================================================
    // TOKEN ISSUING
    // ============================================================
    private AuthTokens issueAuthTokens(User user) {

        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);

        tokenService.saveTokens(user.getId(), access, refresh);

        long expiresInSeconds = ACCESS_TOKEN_TTL.toSeconds();

        return new AuthTokens(
                access,
                refresh,
                expiresInSeconds,
                buildUserResponse(user)
        );
    }


    // ============================================================
    // CONTEXT HELPERS (ADDED)
    // ============================================================

    /**
     * Returns authenticated user ID (or null if not authenticated).
     */
    public UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        return fetchUserIdFromUserDetails(userDetails);
    }

    /**
     * Returns authenticated User. Throws if unauthenticated.
     */
    public User getCurrentUser() {
        UUID id = getCurrentUserId();
        if (id == null) throw new UnauthorizedException();

        return userRepository.findById(id)
                .orElseThrow(UnauthorizedException::new);
    }

    /**
     * Returns true if user is ADMIN.
     */
    public boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return false;
        }

        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Extracts userId from UserDetails (email→User).
     */
    private UUID fetchUserIdFromUserDetails(UserDetails details) {
        String email = details.getUsername();

        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElse(null);
    }
}
