package com.lms.category.repository;

import com.lms.category.entity.Category;
import com.lms.category.entity.CategoryStatus;
import com.lms.course.entity.Course;
import com.lms.course.entity.CourseStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

/** Ref: SRS 6.9, 6.10, 6.11 - optional filters for the category list endpoint. */
public final class CategorySpecifications {

    private CategorySpecifications() {
    }

    public static Specification<Category> nameContains(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern);
    }

    public static Specification<Category> hasStatus(CategoryStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /** Ref: SRS 6.9, 6.11 - public visibility gate: category must have at least one PUBLISHED course. */
    public static Specification<Category> hasPublishedCourse() {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Course> courseRoot = subquery.from(Course.class);
            Join<Course, Category> categoryJoin = courseRoot.join("categories");
            subquery.select(cb.literal(1L))
                    .where(cb.equal(categoryJoin.get("id"), root.get("id")), cb.equal(courseRoot.get("status"), CourseStatus.PUBLISHED));
            return cb.exists(subquery);
        };
    }
}
