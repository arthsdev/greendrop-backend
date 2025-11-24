package br.com.greendrop.backend.infrastructure.security.jwt;

import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.exception.auth.InvalidCredentialsException;
import br.com.greendrop.backend.exception.auth.UnauthorizedException;
import br.com.greendrop.backend.exception.generic.BadRequestException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long jwtRefreshExpiration;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    @Value("${jwt.audience}")
    private String jwtAudience;


    private Key getSigningKey() {
        try {
            return Keys.hmacShaKeyFor(secretKey.getBytes());
        } catch (Exception e) {
            throw new BadRequestException();
        }
    }

    // ------------------------------------------------------
    // TOKEN GENERATION
    // ------------------------------------------------------

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setIssuer(jwtIssuer)
                .setAudience(jwtAudience)
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setIssuer(jwtIssuer)
                .setAudience(jwtAudience)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtRefreshExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    // ------------------------------------------------------
    // VALIDATION + EXTRACTION
    // ------------------------------------------------------

    public String extractUserId(String token) {
        try {
            return parseClaims(token).getSubject();
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException();
        } catch (JwtException e) {
            throw new InvalidCredentialsException();
        }
    }


    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException();
        } catch (JwtException e) {
            throw new InvalidCredentialsException();
        }
    }


    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String userId = extractUserId(token);

            if (!userId.equals(((User) userDetails).getId().toString())) {
                throw new UnauthorizedException();
            }

            return validateToken(token);

        } catch (InvalidCredentialsException | UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidCredentialsException();
        }
    }


    // ------------------------------------------------------
    // PRIVATE - PARSE CLAIMS
    // ------------------------------------------------------

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (ExpiredJwtException e) {
            throw e;
        } catch (JwtException e) {
            throw new InvalidCredentialsException();
        }
    }
}
