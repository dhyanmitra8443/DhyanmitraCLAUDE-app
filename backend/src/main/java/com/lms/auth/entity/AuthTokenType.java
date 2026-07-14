package com.lms.auth.entity;

/** Ref: SRS 3.10, 3.12 - the auth_tokens.token_type CHECK constraint values. */
public enum AuthTokenType {
    REFRESH,
    PASSWORD_RESET
}
