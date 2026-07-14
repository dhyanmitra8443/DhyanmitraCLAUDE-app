package com.lms.auth.dto;

import com.lms.user.entity.User;

import java.util.UUID;

/** Matches openapi.yaml's UserSummary schema. */
public record UserSummary(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String role,
        String status
) {
    public static UserSummary from(User user) {
        return new UserSummary(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name()
        );
    }
}
