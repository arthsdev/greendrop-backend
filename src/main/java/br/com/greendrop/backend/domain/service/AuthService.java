package br.com.greendrop.backend.domain.service;

import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.domain.model.enums.Role;
import br.com.greendrop.backend.domain.repository.UserRepository;
import br.com.greendrop.backend.dto.auth.AuthResponseDTO;
import br.com.greendrop.backend.dto.user.UserRequestDTO;
import br.com.greendrop.backend.dto.user.UserResponseDTO;
import br.com.greendrop.backend.exception.auth.InvalidCredentialsException;
import br.com.greendrop.backend.exception.auth.InvalidTokenException;
import br.com.greendrop.backend.exception.auth.MissingTokenException;
import br.com.greendrop.backend.exception.user.DuplicateResourceException;
import br.com.greendrop.backend.exception.user.ResourceNotFoundException;
import br.com.greendrop.backend.infrastructure.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Handles authentication-related operations:
 * - User registration
 * - Login
 * - Refresh token rotation
 * - Logout
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    // =============================================================
    // REGISTER NEW USER
    // =============================================================
    @Transactional
    public AuthResponseDTO register(UserRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException();
        }

        validatePasswordStrength(request.password());

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .cep(request.cep())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .build();

        userRepository.save(user);

        return generateAuthResponse(user);
    }

    // =============================================================
    // LOGIN
    // =============================================================
    @Transactional(readOnly = true)
    public AuthResponseDTO login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // Invalidate old tokens before generating new ones
        tokenService.deleteAllUserTokens(user.getId().toString());

        return generateAuthResponse(user);
    }

    // =============================================================
    // REFRESH ACCESS TOKEN (ROTATE REFRESH TOKEN)
    // =============================================================
    @Transactional
    public String refreshAccessToken(String oldRefreshToken) {
        if (oldRefreshToken == null || oldRefreshToken.isBlank()) {
            throw new MissingTokenException();
        }

        if (!jwtService.validateToken(oldRefreshToken) || !tokenService.isTokenValid(oldRefreshToken)) {
            throw new InvalidTokenException();
        }

        UUID userId = UUID.fromString(jwtService.extractUserId(oldRefreshToken));
        User user = userRepository.findById(userId)
                .orElseThrow(ResourceNotFoundException::new);

        // Revoke the old refresh token to prevent reuse
        tokenService.revokeToken(oldRefreshToken);

        // Generate new access + refresh tokens
        AuthResponseDTO newTokens = generateAuthResponse(user);

        // Return only the new access token (client stores new refresh token)
        return newTokens.accessToken();
    }

    // =============================================================
    // LOGOUT
    // =============================================================
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new MissingTokenException();
        }

        // Revoke refresh token and blacklist
        tokenService.revokeToken(refreshToken);
        tokenService.blacklistToken(refreshToken);
    }

    // =============================================================
    // HELPERS
    // =============================================================
    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new InvalidCredentialsException();
        }
    }

    private AuthResponseDTO generateAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        tokenService.saveTokens(user.getId(), accessToken, refreshToken);

        return new AuthResponseDTO(accessToken, buildUserResponse(user));
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
