package com.lms.liveclass.repository;

import com.lms.liveclass.entity.LiveClass;
import com.lms.liveclass.entity.LiveClassStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

/** Ref: SRS 11.14 - optional filters for the admin/instructor live class search endpoint. */
public final class LiveClassSpecifications {

    private LiveClassSpecifications() {
    }

    public static Specification<LiveClass> hasCourse(UUID courseId) {
        if (courseId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("courseId"), courseId);
    }

    public static Specification<LiveClass> hasStatus(LiveClassStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<LiveClass> onDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("scheduledDate"), date);
    }
}
