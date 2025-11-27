package br.com.greendrop.backend.infrastructure.security.jwt;

import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.exception.auth.InvalidTokenException;
import br.com.greendrop.backend.exception.auth.TokenExpiredException;
import br.com.greendrop.backend.exception.auth.UnauthorizedException;
import br.com.greendrop.backend.exception.generic.BadRequestException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long accessTokenExpirationMillis;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpirationMillis;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.audience}")
    private String audience;

    // -----------------------------------------------------------------------
    // KEY
    // -----------------------------------------------------------------------

    private Key getSigningKey() {
        try {
            return Keys.hmacShaKeyFor(secretKey.getBytes());
        } catch (Exception e) {
            throw new BadRequestException("Invalid secret key configuration");
        }
    }

    // -----------------------------------------------------------------------
    // TOKEN CREATION (single reusable builder)
    // -----------------------------------------------------------------------

    private String createToken(Map<String, Object> claims, String subject, long expirationMillis) {
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(issuer)
                .setAudience(audience)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMillis))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // -----------------------------------------------------------------------
    // ACCESS TOKEN
    // -----------------------------------------------------------------------

    public String generateAccessToken(User user) {
        Map<String, Object> claims = Map.of(
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "type", "access"
        );

        return createToken(claims, user.getId().toString(), accessTokenExpirationMillis);
    }

    // -----------------------------------------------------------------------
    // REFRESH TOKEN
    // -----------------------------------------------------------------------

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = Map.of(
                "type", "refresh"
        );

        return createToken(claims, user.getId().toString(), refreshTokenExpirationMillis);
    }

    // -----------------------------------------------------------------------
    // CLAIM EXTRACTION
    // -----------------------------------------------------------------------

    public String extractUserId(String token) {
        return getClaims(token).getSubject();
    }

    public String extractEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    public String extractType(String token) {
        return getClaims(token).get("type", String.class);
    }

    // -----------------------------------------------------------------------
    // VALIDATION
    // -----------------------------------------------------------------------

    public boolean validateToken(String token) {
        getClaims(token); // dispara exception se inválido
        return true;
    }

    /**
     * Ensures token belongs to authenticated user.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String tokenEmail = extractEmail(token);

        if (tokenEmail != null && !tokenEmail.equals(userDetails.getUsername())) {
            throw new UnauthorizedException();
        }

        return validateToken(token);
    }

    // -----------------------------------------------------------------------
    // CLAIM PARSING
    // -----------------------------------------------------------------------

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

    // -----------------------------------------------------------------------
    // EXPOSE TOKEN EXPIRATIONS (needed for AuthTokens)
    // -----------------------------------------------------------------------

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMillis / 1000;
    }

    public long getRefreshTokenExpirationSeconds() {
        return refreshTokenExpirationMillis / 1000;
    }
}
