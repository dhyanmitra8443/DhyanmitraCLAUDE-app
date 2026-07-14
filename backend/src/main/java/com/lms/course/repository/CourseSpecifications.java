package com.lms.course.repository;

import com.lms.category.entity.Category;
import com.lms.course.entity.Course;
import com.lms.course.entity.CourseStatus;
import com.lms.course.entity.DifficultyLevel;
import com.lms.user.entity.User;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

/** Ref: SRS 5.13, 5.14 - optional filters for the course list/search endpoint. */
public final class CourseSpecifications {

    private CourseSpecifications() {
    }

    public static Specification<Course> titleContains(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), pattern);
    }

    public static Specification<Course> inCategories(List<UUID> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Course, Category> join = root.join("categories");
            return join.get("id").in(categoryIds);
        };
    }

    public static Specification<Course> hasInstructor(UUID instructorId) {
        if (instructorId == null) {
            return null;
        }
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Course, User> join = root.join("instructors");
            return cb.equal(join.get("id"), instructorId);
        };
    }

    public static Specification<Course> hasDifficulty(DifficultyLevel difficultyLevel) {
        if (difficultyLevel == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("difficultyLevel"), difficultyLevel);
    }

    public static Specification<Course> hasLanguage(String language) {
        if (language == null || language.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get("language")), language.toLowerCase());
    }

    public static Specification<Course> hasStatus(CourseStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
