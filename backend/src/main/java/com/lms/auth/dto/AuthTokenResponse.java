package com.lms.auth.dto;

/** Matches openapi.yaml's AuthTokenResponse schema. */
public record AuthTokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        UserSummary user
) {
}
