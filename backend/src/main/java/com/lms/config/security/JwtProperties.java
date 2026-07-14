package com.lms.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Ref: SRS 3.12, 19.7 - JWT secret and token lifetimes are externalized
 * configuration, never hardcoded (bound from the `app.jwt.*` properties,
 * which come from environment variables in every non-local environment -
 * see application.yml).
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long accessTokenTtlSeconds,
        long refreshTokenTtlSeconds
) {
}
