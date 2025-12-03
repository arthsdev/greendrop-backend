package br.com.greendrop.backend.infrastructure.security.jwt;

import br.com.greendrop.backend.domain.service.TokenService;
import br.com.greendrop.backend.infrastructure.security.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter (stateless).
 *
 * Responsibilities:
 *  - Extract token from header
 *  - Validate signature + expiration (JwtService)
 *  - Check if token is revoked (TokenService)
 *  - Ensure token is ACCESS type
 *  - Load UserDetails and set SecurityContext
 *
 * This filter must remain thin — heavy logic stays in services.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractToken(request.getHeader("Authorization"));

        if (!isTokenUsable(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        authenticate(token, request);

        filterChain.doFilter(request, response);
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private String extractToken(String header) {
        if (header == null) return null;

        if (!header.toLowerCase().startsWith("bearer ")) return null;

        String token = header.substring(7).trim();

        return token.isBlank() ? null : token;
    }

    /**
     * Lightweight validation to determine if a token should proceed to authentication.
     */
    private boolean isTokenUsable(String token) {
        return token != null
                && jwtService.validateToken(token)               // signature + expiration
                && !"refresh".equals(jwtService.extractType(token))  // ensure ACCESS token
                && !tokenService.isTokenRevoked(token);          // blacklist
    }

    private void authenticate(String token, HttpServletRequest request) {
        String email = jwtService.extractEmail(token);
        if (email == null) return;

        // Avoid overriding context if already authenticated
        if (SecurityContextHolder.getContext().getAuthentication() != null) return;

        UserDetails user = userDetailsService.loadUserByUsername(email);

        // Validate ownership (token must belong to same user)
        if (!jwtService.isTokenValid(token, user)) return;

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        user,
                        token,                 // raw token kept as credentials
                        user.getAuthorities()
                );

        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
