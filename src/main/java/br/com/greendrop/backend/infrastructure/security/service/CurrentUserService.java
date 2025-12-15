package br.com.greendrop.backend.infrastructure.security.service;

import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.domain.repository.UserRepository;
import br.com.greendrop.backend.exception.auth.MissingTokenException;
import br.com.greendrop.backend.exception.user.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    /**
     * Gets the current Authentication from Spring Security Context.
     * Ensures there is an authenticated user.
     */
    private Authentication getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new MissingTokenException();
        }

        return auth;
    }

    /**
     * Extracts the current user's ID from Authentication Principal.
     * JwtAuthenticationFilter should place the userId as the principal.
     */
    public UUID getCurrentUserId() {
        Authentication auth = getAuthentication();
        return UUID.fromString(auth.getName()); // principal should be userId
    }

    /**
     * Extracts current user's email from Authentication details or token claims.
     * JwtAuthenticationFilter should set this inside auth.getDetails()
     */
    public String getCurrentUserEmail() {
        Authentication auth = getAuthentication();

        if (auth.getDetails() instanceof String email) {
            return email;
        }

        // If you plan to store the email differently, adjust here.
        throw new MissingTokenException();
    }

    /**
     * Loads the User domain entity using the authenticated user ID.
     */
    public User getCurrentUser() {
        UUID userId = getCurrentUserId();

        return userRepository.findById(userId)
                .orElseThrow(ResourceNotFoundException::new);
    }
}
