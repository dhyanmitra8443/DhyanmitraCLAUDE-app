package com.lms.config.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.UUID;

/**
 * Ref: SRS 2.9, 3.13 - RBAC principal carried on the SecurityContext for
 * every authenticated request. The Spring authority is prefixed with
 * "ROLE_" (Spring Security convention) so hasRole("ADMINISTRATOR") in
 * @PreAuthorize matches the raw ADMINISTRATOR/INSTRUCTOR/STUDENT role
 * values used throughout the SRS and openapi.yaml's x-roles extension.
 */
public class UserPrincipal extends User {

    private final UUID userId;
    private final UUID sessionId;

    public UserPrincipal(UUID userId, String email, String role, UUID sessionId) {
        super(email, "", List.of(authority(role)));
        this.userId = userId;
        this.sessionId = sessionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    private static GrantedAuthority authority(String role) {
        return new SimpleGrantedAuthority("ROLE_" + role);
    }
}
