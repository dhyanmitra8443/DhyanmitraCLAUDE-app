package com.lms.resource.repository;

import com.lms.resource.entity.LessonResource;
import com.lms.resource.entity.ResourceStatus;
import com.lms.resource.entity.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LessonResourceRepository extends JpaRepository<LessonResource, UUID> {

    List<LessonResource> findByLessonIdOrderByDisplayOrderAsc(UUID lessonId);

    long countByLessonId(UUID lessonId);

    boolean existsByLessonIdAndResourceTypeAndStatus(UUID lessonId, ResourceType resourceType, ResourceStatus status);

    long countByLessonIdAndResourceTypeAndStatus(UUID lessonId, ResourceType resourceType, ResourceStatus status);
}
