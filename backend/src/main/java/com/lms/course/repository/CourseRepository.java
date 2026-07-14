package com.lms.course.repository;

import com.lms.course.entity.Course;
import com.lms.course.entity.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID>, JpaSpecificationExecutor<Course> {

    boolean existsByTitle(String title);

    boolean existsByTitleAndIdNot(String title, UUID id);

    long countByCategories_Id(UUID categoryId);

    long countByStatus(CourseStatus status);

    List<Course> findByInstructors_Id(UUID instructorId);

    long countByStatusAndInstructors_Id(CourseStatus status, UUID instructorId);

    long countByCreatedAtBetween(java.time.OffsetDateTime from, java.time.OffsetDateTime to);
}
