package com.lms.progress.repository;

import com.lms.progress.entity.CourseCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseCompletionRepository extends JpaRepository<CourseCompletion, UUID> {

    Optional<CourseCompletion> findByStudentIdAndCourseId(UUID studentId, UUID courseId);

    List<CourseCompletion> findByStudentId(UUID studentId);

    long countByCourseIdIn(List<UUID> courseIds);

    long countByCompletedAtBetween(OffsetDateTime from, OffsetDateTime to);
}
