package com.lms.auth.dto;

/** Ref: SRS 3.5. Matches openapi.yaml's GET /auth/instructors/invitations/{token} response data. */
public record InvitationPreviewResponse(
        String email,
        String firstName,
        String lastName
) {
}
