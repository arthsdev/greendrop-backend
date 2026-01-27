package br.com.greendrop.backend.infrastructure.security.config;

import br.com.greendrop.backend.infrastructure.security.jwt.JwtAuthEntryPoint;
import br.com.greendrop.backend.infrastructure.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final CorsConfigurationSource corsConfigurationSource;

    // =====================================================
    // SECURITY FILTER CHAIN
    // =====================================================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Disable defaults not used in JWT
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Stateless API
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Custom auth error handling (401)
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(jwtAuthEntryPoint)
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth

                        // =========================================
                        // PUBLIC ENDPOINTS
                        // =========================================
                        .requestMatchers(
                                "/api/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // =========================================
                        // PRODUCT CLAIM FLOW
                        // =========================================
                        .requestMatchers(HttpMethod.POST, "/api/products/*/claim")
                        .authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/products/*/unclaim")
                        .hasRole("COLLECTOR")

                        .requestMatchers(HttpMethod.GET, "/api/products/me/claims")
                        .hasRole("COLLECTOR")

                        // =========================================
                        // AUTHENTICATED USER CONTEXT
                        // =========================================
                        .requestMatchers(HttpMethod.GET, "/api/products/me")
                        .authenticated()

                        // =========================================
                        // READ PRODUCTS
                        // =========================================
                        .requestMatchers(HttpMethod.GET, "/api/products/**")
                        .hasAnyRole("COLLECTOR", "ADMIN")

                        // =========================================
                        // CREATE PRODUCT
                        // =========================================
                        .requestMatchers(HttpMethod.POST, "/api/products/**")
                        .hasAnyRole("USER", "ADMIN")

                        // =========================================
                        // UPDATE / DELETE PRODUCT
                        // =========================================
                        .requestMatchers(HttpMethod.PATCH, "/api/products/**")
                        .authenticated()

                        .requestMatchers(HttpMethod.DELETE, "/api/products/**")
                        .authenticated()

                        // =========================================
                        // FALLBACK
                        // =========================================
                        .anyRequest().authenticated()
                )

                // JWT filter BEFORE UsernamePasswordAuthenticationFilter
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // =====================================================
    // AUTHENTICATION MANAGER (USED BY AuthService)
    // =====================================================
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
