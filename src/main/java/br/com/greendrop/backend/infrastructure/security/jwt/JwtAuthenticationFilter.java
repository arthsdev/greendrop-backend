package br.com.greendrop.backend.infrastructure.security.jwt;

import br.com.greendrop.backend.domain.service.TokenService;
import br.com.greendrop.backend.infrastructure.security.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT authentication filter (stateless).
 *
 * Responsibilities:
 *  - Extract token from Authorization header
 *  - Validate signature + expiration
 *  - Ensure token is ACCESS type
 *  - Build Spring Security Authentication with ROLE_*
 *  - Populate SecurityContext
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenService tokenService;
    private final CustomUserDetailsService userDetailsService;

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

        authenticate(token);

        filterChain.doFilter(request, response);
    }

    // ============================================================
    // TOKEN EXTRACTION
    // ============================================================

    private String extractToken(String header) {
        if (header == null) return null;
        if (!header.toLowerCase().startsWith("bearer ")) return null;

        String token = header.substring(7).trim();
        return token.isBlank() ? null : token;
    }

    // ============================================================
    // TOKEN VALIDATION
    // ============================================================

    private boolean isTokenUsable(String token) {
        if (token == null) return false;

        // Signature + expiration
        if (!jwtService.validateToken(token)) return false;

        String type;
        try {
            type = jwtService.extractType(token);
        } catch (Exception e) {
            return false;
        }

        // Only ACCESS tokens authenticate requests
        if ("access".equals(type)) {
            return true;
        }

        // Refresh tokens should NEVER authenticate endpoints
        return false;
    }

    // ============================================================
    // AUTHENTICATION
    // ============================================================

    private void authenticate(String token) {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        String email = jwtService.extractEmail(token);
        String userId = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token); // USER, ADMIN, etc

        if (email == null || userId == null || role == null) return;

        // Ensure user still exists (optional but recommended)
        userDetailsService.loadUserByUsername(email);

        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + role));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        userId,      // principal
                        null,
                        authorities
                );

        auth.setDetails(email);

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ============================================================
    // FILTER SKIP
    // ============================================================

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/api/auth/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui");
    }
}
