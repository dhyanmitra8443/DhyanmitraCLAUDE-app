package com.lms.lesson.repository;

import com.lms.lesson.entity.ContentStatus;
import com.lms.lesson.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    List<Lesson> findBySectionIdOrderByDisplayOrderAsc(UUID sectionId);

    long countBySectionId(UUID sectionId);

    long countByCourseIdAndStatus(UUID courseId, ContentStatus status);

    List<Lesson> findByCourseIdAndStatus(UUID courseId, ContentStatus status);

    boolean existsBySectionIdAndStatus(UUID sectionId, ContentStatus status);

    boolean existsByCourseIdAndStatus(UUID courseId, ContentStatus status);

    boolean existsBySectionIdAndTitle(UUID sectionId, String title);

    boolean existsBySectionIdAndTitleAndIdNot(UUID sectionId, String title, UUID id);

    Optional<Lesson> findByCourseIdAndIsPreviewTrue(UUID courseId);
}
