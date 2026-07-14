package com.lms.progress.repository;

import com.lms.progress.entity.LessonProgress;
import com.lms.progress.entity.ProgressStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, UUID> {

    Optional<LessonProgress> findByStudentIdAndLessonId(UUID studentId, UUID lessonId);

    List<LessonProgress> findByStudentIdAndLessonIdIn(UUID studentId, List<UUID> lessonIds);

    @Query("select count(lp) from LessonProgress lp where lp.studentId = :studentId and lp.status = :status and lp.lessonId in :lessonIds")
    long countByStudentIdAndStatusAndLessonIdIn(
            @Param("studentId") UUID studentId, @Param("status") ProgressStatus status, @Param("lessonIds") List<UUID> lessonIds);

    long countByStatusAndCompletedAtBetween(ProgressStatus status, java.time.OffsetDateTime from, java.time.OffsetDateTime to);

    long countByStatus(ProgressStatus status);
}
