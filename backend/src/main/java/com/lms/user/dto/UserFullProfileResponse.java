package com.lms.user.dto;

import com.lms.user.entity.User;
import com.lms.user.entity.UserRole;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Matches openapi.yaml's UserFullProfile schema (Ref: SRS 4.12).
 *
 * subscriptions/certificatesEarned/assignedCourses/studentCount depend on
 * the Subscription (Ch.9), Certificate (Ch.12), and Course (Ch.5) modules,
 * none of which exist yet - they are always empty/zero here until those
 * chapters are implemented and this factory is wired up to read from them.
 */
public record UserFullProfileResponse(
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
        OffsetDateTime updatedAt,
        List<Object> subscriptions,
        List<Object> certificatesEarned,
        List<Object> assignedCourses,
        Integer studentCount
) {
    public static UserFullProfileResponse from(User user) {
        boolean isStudent = user.getRole() == UserRole.STUDENT;
        boolean isInstructor = user.getRole() == UserRole.INSTRUCTOR;
        return new UserFullProfileResponse(
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
                user.getUpdatedAt(),
                isStudent ? List.of() : null,
                isStudent ? List.of() : null,
                isInstructor ? List.of() : null,
                isInstructor ? 0 : null
        );
    }
}
