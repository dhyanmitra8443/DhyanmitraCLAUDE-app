package com.lms.course.entity;

import com.lms.category.entity.Category;
import com.lms.shared.entity.BaseEntity;
import com.lms.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Ref: SRS Chapter 5 - Course Management.
 *
 * instructors/categories stay LAZY (not EAGER like User.specializations) -
 * CourseSummary embeds both in every row of a paginated list, and eagerly
 * fetching a collection alongside pagination produces wrong/duplicated
 * results in Hibernate. @BatchSize keeps that from becoming N+1 queries;
 * CourseService is responsible for touching these collections inside its
 * @Transactional methods, before mapping to DTOs.
 */
@Getter
@Setter
@Entity
@Table(name = "courses")
public class Course extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(name = "short_description", nullable = false)
    private String shortDescription;

    @Column(name = "detailed_description", nullable = false)
    private String detailedDescription;

    @Column(name = "thumbnail_url", nullable = false)
    private String thumbnailUrl;

    @Column(nullable = false)
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false)
    private DifficultyLevel difficultyLevel;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status = CourseStatus.DRAFT;

    // No JPA relation: lessons (Ch.7) don't exist yet, and the DB FK itself
    // is added later by a future migration for the same reason (see V1's
    // comment on this column).
    @Column(name = "preview_lesson_id")
    private UUID previewLessonId;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @BatchSize(size = 20)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "course_instructors",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "instructor_id")
    )
    private Set<User> instructors = new HashSet<>();

    @BatchSize(size = 20)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "course_categories",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();
}
