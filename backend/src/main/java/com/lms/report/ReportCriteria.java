package com.lms.report;

import com.lms.user.entity.UserRole;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Ref: SRS 15.7 (filtering), 15.10 (search), 15.13 (role/ownership scoping).
 *
 * callerId and callerRole are not user-supplied filters - they are the
 * scoping rule itself: a student's report can only ever read that student's
 * rows, and an instructor's can only read their own courses'. Keeping them
 * in the same object as the optional filters means no query path can forget
 * to apply them.
 */
public record ReportCriteria(
        UUID callerId,
        UserRole callerRole,
        LocalDate dateFrom,
        LocalDate dateTo,
        UUID courseId,
        String search
) {

    public boolean hasSearch() {
        return search != null && !search.isBlank();
    }
}
