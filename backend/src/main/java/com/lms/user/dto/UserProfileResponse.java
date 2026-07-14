package com.lms.user.dto;

import com.lms.user.entity.User;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

/** Matches openapi.yaml's UserProfile schema (Ref: SRS 4.3). */
public record UserProfileResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String mobileNumber,
        String profilePhotoUrl,
        LocalDate dateOfBirth,
        String gender,
        String role,
        String status,
        String professionalBio,
        Integer yearsOfExperience,
        Set<String> specializations,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getProfilePhotoUrl(),
                user.getDateOfBirth(),
                user.getGender(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getProfessionalBio(),
                user.getYearsOfExperience(),
                user.getSpecializations(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
