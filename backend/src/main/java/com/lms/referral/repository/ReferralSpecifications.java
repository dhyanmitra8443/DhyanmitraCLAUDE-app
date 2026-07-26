package com.lms.referral.repository;

import com.lms.referral.entity.Referral;
import com.lms.referral.entity.ReferralStatus;
import org.springframework.data.jpa.domain.Specification;

/** Optional filters for the administrator-only cross-referrer list endpoint. */
public final class ReferralSpecifications {

    private ReferralSpecifications() {
    }

    public static Specification<Referral> hasStatus(ReferralStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /** Matches the referee's email, first name, or last name (case-insensitive). */
    public static Specification<Referral> refereeMatches(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("refereeEmail").as(String.class)), pattern),
                cb.like(cb.lower(root.get("refereeFirstName")), pattern),
                cb.like(cb.lower(root.get("refereeLastName")), pattern)
        );
    }
}
