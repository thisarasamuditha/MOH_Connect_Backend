package com.moh.moh_backend.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * Minimal JWT service for token generation and validation using JJWT.
 * No Spring Security integration as requested.
 */
@Service
public class JwtService {
    // HS256 secret in Base64 form loaded from application.properties
    private final SecretKey key;

    // Default token validity 24h
    private final long defaultTtlMillis;

    public JwtService(
            @Value("${app.jwt.secret-base64}") String secretBase64,
            @Value("${app.jwt.ttl-millis:86400000}") long ttlMillis
    ) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
        this.defaultTtlMillis = ttlMillis;
    }

    /** Generate a JWT with standard claims. */
    public String generateToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(new Date(now.toEpochMilli() + defaultTtlMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Validate token signature and expiry; returns true if valid. */
    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getRole(String token) {
        try {
            var claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload();
            Object role = claims.get("role");
            return role != null ? role.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public Integer getUserId(String token) {
        try {
            var claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload();
            Object userId = claims.get("userId");
            if (userId == null) return null;
            if (userId instanceof Integer) return (Integer) userId;
            return Integer.valueOf(userId.toString());
        } catch (Exception e) {
            return null;
        }
    }

}
