package br.com.greendrop.backend.infrastructure.security.service;

import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.domain.repository.UserRepository;
import br.com.greendrop.backend.exception.auth.InvalidTokenException;
import br.com.greendrop.backend.exception.auth.MissingTokenException;
import br.com.greendrop.backend.exception.user.ResourceNotFoundException;
import br.com.greendrop.backend.infrastructure.security.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final HttpServletRequest request;

    // ============================================================
    // INTERNAL – EXTRACT RAW TOKEN FROM REQUEST HEADER
    // ============================================================
    private String extractRawToken() {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new MissingTokenException();
        }

        String token = authHeader.substring(7);

        if (!jwtService.validateToken(token)) {
            throw new InvalidTokenException();
        }

        return token;
    }

    // ============================================================
    // PUBLIC UTILITIES
    // ============================================================

    public UUID getCurrentUserId() {
        String token = extractRawToken();
        return UUID.fromString(jwtService.extractUserId(token));
    }

    public String getCurrentUserEmail() {
        return jwtService.extractEmail(extractRawToken());
    }

    public User getCurrentUser() {
        UUID id = getCurrentUserId();

        return userRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
    }
}
