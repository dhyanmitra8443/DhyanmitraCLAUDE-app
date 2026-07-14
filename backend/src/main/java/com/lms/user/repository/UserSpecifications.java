package com.lms.user.repository;

import com.lms.user.entity.User;
import com.lms.user.entity.UserRole;
import com.lms.user.entity.UserStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/** Ref: SRS 4.9, 4.10, 4.11 - optional filters for the admin user search/list endpoint. */
public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> search(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> {
            Predicate name = cb.like(cb.lower(cb.concat(cb.concat(root.get("firstName"), " "), root.get("lastName"))), pattern);
            Predicate email = cb.like(cb.lower(root.get("email").as(String.class)), pattern);
            Predicate mobile = cb.like(root.get("mobileNumber"), pattern);
            return cb.or(name, email, mobile);
        };
    }

    public static Specification<User> hasRole(UserRole role) {
        if (role == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("role"), role);
    }

    public static Specification<User> hasStatus(UserStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<User> registeredAfter(LocalDate date) {
        if (date == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), date.atStartOfDay().atOffset(ZoneOffset.UTC));
    }

    public static Specification<User> registeredBefore(LocalDate date) {
        if (date == null) {
            return null;
        }
        OffsetDateTime endOfDay = date.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        return (root, query, cb) -> cb.lessThan(root.get("createdAt"), endOfDay);
    }
}
