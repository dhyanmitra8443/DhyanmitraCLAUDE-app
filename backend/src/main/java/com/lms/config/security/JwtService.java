package com.lms.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Ref: SRS 3.3, 3.12 - issues and validates JWT access/refresh tokens.
 * Token payload intentionally carries only userId/email/role - never a
 * password or other sensitive data (Ref: SRS 17.9, tokens travel in the
 * Authorization header and must not leak information if decoded client-side).
 */
@Service
public class JwtService {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    private static final String CLAIM_SESSION_ID = "sid";

    private final SecretKey signingKey;
    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes());
    }

    public String generateAccessToken(UUID userId, String email, String role, UUID sessionId) {
        return buildToken(userId, email, role, sessionId, "ACCESS", properties.accessTokenTtlSeconds());
    }

    public String generateRefreshToken(UUID userId, String email, String role, UUID sessionId) {
        return buildToken(userId, email, role, sessionId, "REFRESH", properties.refreshTokenTtlSeconds());
    }

    private String buildToken(UUID userId, String email, String role, UUID sessionId, String tokenType, long ttlSeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_SESSION_ID, sessionId.toString())
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(signingKey)
                .compact();
    }

    /** Returns the parsed claims if, and only if, the token is well-formed, signed correctly, and unexpired. */
    public Optional<Claims> parseAndValidate(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException ex) {
            // Ref: SRS 3.15 - "Reject expired or revoked tokens" without leaking why.
            return Optional.empty();
        }
    }

    public UUID extractUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    public String extractEmail(Claims claims) {
        return claims.get(CLAIM_EMAIL, String.class);
    }

    public String extractRole(Claims claims) {
        return claims.get(CLAIM_ROLE, String.class);
    }

    public UUID extractSessionId(Claims claims) {
        return UUID.fromString(claims.get(CLAIM_SESSION_ID, String.class));
    }

    public boolean isAccessToken(Claims claims) {
        return "ACCESS".equals(claims.get(CLAIM_TOKEN_TYPE, String.class));
    }

    public boolean isRefreshToken(Claims claims) {
        return "REFRESH".equals(claims.get(CLAIM_TOKEN_TYPE, String.class));
    }
}
