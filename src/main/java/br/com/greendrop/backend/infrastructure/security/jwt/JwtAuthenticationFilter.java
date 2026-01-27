package br.com.greendrop.backend.infrastructure.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "bearer ";
    private static final String ACCESS_TOKEN_TYPE = "access";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        extractToken(request)
                .filter(this::isAccessToken)
                .ifPresent(this::authenticate);

        filterChain.doFilter(request, response);
    }

    // ============================================================
    // TOKEN EXTRACTION
    // ============================================================

    private java.util.Optional<String> extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null) return java.util.Optional.empty();
        if (!header.toLowerCase().startsWith(BEARER_PREFIX)) return java.util.Optional.empty();

        String token = header.substring(7).trim();
        return token.isBlank()
                ? java.util.Optional.empty()
                : java.util.Optional.of(token);
    }

    // ============================================================
    // TOKEN VALIDATION
    // ============================================================

    private boolean isAccessToken(String token) {
        if (!jwtService.validateToken(token)) {
            return false;
        }

        return ACCESS_TOKEN_TYPE.equalsIgnoreCase(
                jwtService.extractType(token)
        );
    }

    // ============================================================
    // AUTHENTICATION
    // ============================================================

    private void authenticate(String token) {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        String userId = jwtService.extractUserId(token);
        String email  = jwtService.extractEmail(token);
        String role   = jwtService.extractRole(token);

        if (userId == null || email == null || role == null) {
            return;
        }

        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
        );

        /*
         * Authentication Contract:
         * - principal = userId (UUID as String)
         * - credentials = null
         * - details = email
         */
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        authorities
                );

        authentication.setDetails(email);

        SecurityContextHolder.getContext().setAuthentication(authentication);
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
