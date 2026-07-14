package com.lms.lesson.repository;

import com.lms.lesson.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SectionRepository extends JpaRepository<Section, UUID> {

    List<Section> findByCourseIdOrderByDisplayOrderAsc(UUID courseId);

    long countByCourseId(UUID courseId);

    boolean existsByCourseIdAndTitle(UUID courseId, String title);

    boolean existsByCourseIdAndTitleAndIdNot(UUID courseId, String title, UUID id);
}
