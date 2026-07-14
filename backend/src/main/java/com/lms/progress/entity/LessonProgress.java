package com.lms.progress.entity;

import com.lms.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Ref: SRS 12.3, 12.4 - manual, per-lesson, idempotent completion (Version 1 has no auto-tracking). */
@Getter
@Setter
@Entity
@Table(name = "lesson_progress")
public class LessonProgress extends BaseEntity {

    @Column(name = "student_id", nullable = false, updatable = false)
    private UUID studentId;

    @Column(name = "lesson_id", nullable = false, updatable = false)
    private UUID lessonId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgressStatus status = ProgressStatus.NOT_STARTED;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;
}
