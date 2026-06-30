package com.attendance.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT factory and parser — creates and validates HS256-signed tokens.
 * <p>Access tokens (short-lived, ~15 min) and refresh tokens (long-lived, ~7 days)
 * both carry the user email as subject and role as a custom claim.
 * Each token includes a unique JTI (JWT ID) for blacklisting support.
 * Configuration: jwt.secret, jwt.access-token-expiration, jwt.refresh-token-expiration.
 * Layer: security.</p>
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessExpirationMs; // e.g., 900000 (15 min)

    @Value("${jwt.refresh-token-expiration}")
    private long refreshExpirationMs; // e.g., 604800000 (7 days)

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Create a short-lived access token with a unique JTI.
     * @param subject user email
     * @param role user role as string
     * @return signed JWT string
     */
    public String createAccessToken(String subject, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessExpirationMs);
        return Jwts.builder()
                .setSubject(subject)
                .claim("role", role)
                .setId(java.util.UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Create a long-lived refresh token (used in token rotation).
     * @param subject user email
     * @param role user role as string
     * @return signed JWT string
     */
    public String createRefreshToken(String subject, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshExpirationMs);
        return Jwts.builder()
                .setSubject(subject)
                .claim("role", role)
                .setId(java.util.UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Parse and validate a JWT. Throws on invalid signature or expiry.
     * @param token raw JWT string
     * @return parsed claims
     */
    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token);
    }
}
