package br.com.greendrop.backend.infrastructure.security.jwt;

import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.exception.auth.InvalidTokenException;
import br.com.greendrop.backend.exception.auth.TokenExpiredException;
import br.com.greendrop.backend.exception.auth.UnauthorizedException;
import br.com.greendrop.backend.exception.generic.BadRequestException;
import br.com.greendrop.backend.exception.global.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JwtService
 *
 * Provides utilities for creating and validating JWT tokens:
 * - Access tokens (short-lived, include email + role)
 * - Refresh tokens (long-lived, include a JTI for rotation)
 *
 * This service does NOT store tokens itself. Instead, it:
 * 1. Generates JWT tokens.
 * 2. Extracts token claims.
 * 3. Ensures tokens are valid and not expired.
 *
 * Integration with the TokenService:
 * - TokenService is responsible for storing the SHA-256 hash of JTI (refresh tokens).
 * - JwtService simply generates tokens and exposes their metadata.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long accessTokenExpirationMillis;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpirationMillis;

    @Value("${jwt.issuer:greendrop}")
    private String issuer;

    @Value("${jwt.audience:greendrop-client}")
    private String audience;

    // ======================================================================
    // Key generation
    // ======================================================================

    /**
     * Returns the signing key used for HMAC SHA-256.
     * Throws a domain exception if the key is invalid or improperly sized.
     */
    private Key getSigningKey() {
        try {
            return Keys.hmacShaKeyFor(secretKey.getBytes());
        } catch (Exception e) {
            throw new BadRequestException(ErrorCode.INVALID_SECRET_KEY);
        }
    }

    // ======================================================================
    // Token builder
    // ======================================================================

    /**
     * Generic JWT token creation method.
     *
     * @param claims custom claims to embed in the token
     * @param subject typically the userId
     * @param expirationMillis token validity duration
     * @return signed JWT token
     */
    private String createToken(Map<String, Object> claims, String subject, long expirationMillis) {
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)                         // custom payload
                .setSubject(subject)                       // user id
                .setIssuer(issuer)                         // identifies our platform
                .setAudience(audience)                     // identifies our client app
                .setIssuedAt(now)                          // issued time stamp
                .setExpiration(new Date(now.getTime() + expirationMillis))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ======================================================================
    // Access Token
    // ======================================================================

    /**
     * Generates a short-lived access token.
     *
     * Contains:
     * - email
     * - role
     * - type = "access"
     *
     * @param user domain user
     * @return signed access token
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().name());
        claims.put("type", "access");

        return createToken(
                claims,
                user.getId().toString(),
                accessTokenExpirationMillis
        );
    }

    // ======================================================================
    // Refresh Token
    // ======================================================================

    /**
     * Generates a refresh token that MUST contain a unique JTI (JWT ID).
     * The TokenService is responsible for generating and storing the JTI hash.
     *
     * @param user the user
     * @param jti  unique token identifier (UUID)
     * @return signed refresh token
     */
    public String generateRefreshToken(User user, String jti) {
        if (jti == null) {
            throw new IllegalArgumentException("Refresh token requires a non-null JTI");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("jti", jti);  // required for token rotation

        return createToken(
                claims,
                user.getId().toString(),
                refreshTokenExpirationMillis
        );
    }

    // ======================================================================
    // Claim extractors
    // ======================================================================

    /** Extracts the userId (subject). */
    public String extractUserId(String token) {
        return getClaims(token).getSubject();
    }

    /** Extracts the e-mail from the JWT. */
    public String extractEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    /** Extracts the "type" claim ("access" or "refresh"). */
    public String extractType(String token) {
        return getClaims(token).get("type", String.class);
    }

    /** Extracts the JTI from a refresh token. */
    public String extractJti(String token) {
        return getClaims(token).get("jti", String.class);
    }

    /** Generates a new UUID for refresh token rotation. */
    public String generateJti() {
        return UUID.randomUUID().toString();
    }

    // ======================================================================
    // Validation
    // ======================================================================

    /**
     * Validates the JWT by attempting to parse its claims.
     * If any parsing error occurs, domain-specific exceptions are thrown.
     */
    public boolean validateToken(String token) {
        getClaims(token);
        return true;
    }

    /**
     * Validates the JWT and ensures it matches the provided UserDetails.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String email = extractEmail(token);

        // UserDetails.getUsername() stores the email
        if (email != null && !email.equals(userDetails.getUsername())) {
            throw new UnauthorizedException();
        }

        return validateToken(token);
    }

    /**
     * Extracts all claims from the token.
     * Throws TokenExpiredException or InvalidTokenException accordingly.
     */
    private Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException();
        } catch (JwtException e) {
            throw new InvalidTokenException();
        }
    }

    // ======================================================================
    // Expiration Helpers
    // ======================================================================

    /** @return access token lifetime in seconds */
    public long getAccessTokenExpirationSeconds() {
        return Math.max(1, accessTokenExpirationMillis / 1000);
    }

    /** @return refresh token lifetime in seconds */
    public long getRefreshTokenExpirationSeconds() {
        return Math.max(1, refreshTokenExpirationMillis / 1000);
    }
}