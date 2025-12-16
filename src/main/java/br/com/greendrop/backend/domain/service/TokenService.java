package br.com.greendrop.backend.domain.service;

import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.infrastructure.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtService jwtService;
    private final StringRedisTemplate redis;

    // TTL based on refresh token expiration time
    private Duration refreshTokenTtl() {
        return Duration.ofSeconds(jwtService.getRefreshTokenExpirationSeconds());
    }

    // =========================================================================
    // CREATE
    // =========================================================================
    /**
     * Creates a new refresh token, stores SHA-256(refresh) in Redis and returns raw token.
     */
    public String createAndStoreRefreshToken(User user) {

        String jti = jwtService.generateJti();
        String token = jwtService.generateRefreshToken(user, jti);

        // store SHA256(refreshToken)
        String tokenHash = sha256Hex(token);

        // Redis key: refresh:<jti>
        redis.opsForValue().set("refresh:" + jti, tokenHash, refreshTokenTtl());

        return token;
    }

    // =========================================================================
    // VALIDATION
    // =========================================================================
    /**
     * A refresh token is valid only if:
     *  - JWT is structurally valid
     *  - type == refresh
     *  - its SHA-256 hash matches stored hash in Redis
     */
    public boolean isRefreshTokenValid(String refreshToken) {
        try {
            jwtService.validateToken(refreshToken);
        } catch (Exception e) {
            return false;
        }

        if (!"refresh".equals(jwtService.extractType(refreshToken)))
            return false;

        String jti = jwtService.extractJti(refreshToken);
        if (jti == null || jti.isBlank()) return false;

        String storedHash = redis.opsForValue().get("refresh:" + jti);
        if (storedHash == null) return false;

        return storedHash.equals(sha256Hex(refreshToken));
    }

    // =========================================================================
    // ROTATION
    // =========================================================================
    /**
     * Rotation:
     *  - validates old token
     *  - deletes old SHA-256(refresh)
     *  - creates and stores a new refresh
     */
    public String rotateRefreshToken(String oldRefreshToken, User user) {
        if (!isRefreshTokenValid(oldRefreshToken))
            throw new IllegalArgumentException("Invalid refresh token");

        String oldJti = jwtService.extractJti(oldRefreshToken);

        // delete old jti
        redis.delete("refresh:" + oldJti);

        return createAndStoreRefreshToken(user);
    }

    // =========================================================================
    // REVOKE
    // =========================================================================
    /**
     * Revoke refresh token by deleting its Redis entry.
     */
    public void revokeRefreshToken(String refreshToken) {
        try {
            String jti = jwtService.extractJti(refreshToken);
            if (jti != null)
                redis.delete("refresh:" + jti);
        } catch (Exception ignored) {}
    }

    // =========================================================================
    // SHA256 helper
    // =========================================================================
    private String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("Unable to compute SHA-256", e);
        }
    }
}