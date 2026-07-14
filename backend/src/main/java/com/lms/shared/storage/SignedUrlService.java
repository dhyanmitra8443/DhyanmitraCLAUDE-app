package com.lms.shared.storage;

import com.lms.config.security.JwtProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

/**
 * Ref: SRS 8.8, 8.15, 17.24 - stateless, time-limited, HMAC-signed tokens
 * standing in for real cloud-storage pre-signed URLs (S3, etc.) until this
 * dev-stage local storage is swapped for the real thing. Reuses the JWT
 * signing secret rather than adding a second one to configure/rotate.
 */
@Service
public class SignedUrlService {

    private final SecretKeySpec key;

    public SignedUrlService(JwtProperties jwtProperties) {
        this.key = new SecretKeySpec(jwtProperties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    public String issue(String subject, Duration ttl) {
        long expiresAt = Instant.now().plus(ttl).getEpochSecond();
        String payload = subject + ":" + expiresAt;
        String token = payload + ":" + sign(payload);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    /** Returns the subject if, and only if, the token's signature is valid and it hasn't expired. */
    public Optional<String> validate(String token) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":", 3);
            if (parts.length != 3) {
                return Optional.empty();
            }
            String subject = parts[0];
            long expiresAt = Long.parseLong(parts[1]);
            String signature = parts[2];

            if (!sign(subject + ":" + expiresAt).equals(signature)) {
                return Optional.empty();
            }
            if (Instant.now().getEpochSecond() > expiresAt) {
                return Optional.empty();
            }
            return Optional.of(subject);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC signing failed", e);
        }
    }
}
