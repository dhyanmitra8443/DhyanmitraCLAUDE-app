package com.lms.progress.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Ref: SRS 12.9 - persisted completion fact (course is complete once every
 * currently-PUBLISHED lesson has a COMPLETED LessonProgress row), so
 * certificate triggers don't require recomputing progress from scratch.
 * No updated_at column exists, so this does not extend BaseEntity.
 */
@Getter
@Setter
@Entity
@Table(name = "course_completions")
public class CourseCompletion {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "student_id", nullable = false, updatable = false)
    private UUID studentId;

    @Column(name = "course_id", nullable = false, updatable = false)
    private UUID courseId;

    @Column(name = "completed_at", nullable = false, updatable = false)
    private OffsetDateTime completedAt;
}
