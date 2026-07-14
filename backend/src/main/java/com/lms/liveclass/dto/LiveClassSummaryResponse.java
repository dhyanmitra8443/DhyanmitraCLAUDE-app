package com.lms.liveclass.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Matches openapi.yaml's LiveClassSummary schema (Ref: SRS Chapter 11).
 * meetingUrl is nulled out by LiveClassService for callers who aren't
 * authorized to see it (admin/assigned-instructor/actively-subscribed
 * student only), per the schema's own field description - the rest of
 * the summary stays visible to any authenticated caller.
 */
public record LiveClassSummaryResponse(
        UUID id,
        UUID courseId,
        String title,
        String description,
        LocalDate scheduledDate,
        LocalTime scheduledTime,
        String meetingUrl,
        String recordingUrl,
        String status
) {
}
