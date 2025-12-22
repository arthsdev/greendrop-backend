package br.com.greendrop.backend.infrastructure.security.config;

import br.com.greendrop.backend.infrastructure.security.jwt.JwtAuthEntryPoint;
import br.com.greendrop.backend.infrastructure.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(jwtAuthEntryPoint)
                )
                .authorizeHttpRequests(auth -> auth

                        // =====================================================
                        // PUBLIC ENDPOINTS
                        // =====================================================
                        .requestMatchers(
                                "/api/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // =====================================================
                        // PRODUCT CLAIM FLOW (COLLECTOR ONLY)
                        // =====================================================
                        .requestMatchers(HttpMethod.POST, "/api/products/*/claim")
                        .hasRole("COLLECTOR")

                        .requestMatchers(HttpMethod.POST, "/api/products/*/unclaim")
                        .hasRole("COLLECTOR")

                        .requestMatchers(HttpMethod.GET, "/api/products/me/claims")
                        .hasRole("COLLECTOR")

                        // =====================================================
                        // AUTHENTICATED USER CONTEXT
                        // =====================================================
                        .requestMatchers(HttpMethod.GET, "/api/products/me")
                        .authenticated()

                        // =====================================================
                        // READ PRODUCTS
                        // =====================================================
                        .requestMatchers(HttpMethod.GET, "/api/products/**")
                        .hasAnyRole("COLLECTOR", "ADMIN")

                        // =====================================================
                        // CREATE PRODUCT
                        // (Collectors are NOT allowed)
                        // =====================================================
                        .requestMatchers(HttpMethod.POST, "/api/products/**")
                        .hasAnyRole("USER", "ADMIN")

                        // =====================================================
                        // UPDATE / DELETE PRODUCT
                        // (Ownership checked in service layer)
                        // =====================================================
                        .requestMatchers(HttpMethod.PATCH, "/api/products/**")
                        .authenticated()

                        .requestMatchers(HttpMethod.DELETE, "/api/products/**")
                        .authenticated()

                        // =====================================================
                        // FALLBACK
                        // =====================================================
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
