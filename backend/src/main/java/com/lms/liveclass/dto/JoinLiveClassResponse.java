package com.lms.liveclass.dto;

/** Ref: SRS 11.8, 11.9, 11.10. */
public record JoinLiveClassResponse(
        String meetingUrl,
        String meetingPassword
) {
}
