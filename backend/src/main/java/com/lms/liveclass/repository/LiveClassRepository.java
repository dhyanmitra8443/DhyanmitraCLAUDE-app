package com.lms.liveclass.repository;

import com.lms.liveclass.entity.LiveClass;
import com.lms.liveclass.entity.LiveClassStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LiveClassRepository extends JpaRepository<LiveClass, UUID>, JpaSpecificationExecutor<LiveClass> {

    Page<LiveClass> findByCourseId(UUID courseId, Pageable pageable);

    long countByStatus(LiveClassStatus status);

    long countByCourseIdIn(List<UUID> courseIds);

    long countByCourseIdInAndStatus(List<UUID> courseIds, LiveClassStatus status);

    long countByCourseIdInAndStatusAndScheduledDateGreaterThanEqual(List<UUID> courseIds, LiveClassStatus status, LocalDate onOrAfter);

    Optional<LiveClass> findFirstByCourseIdAndStatusAndScheduledDateGreaterThanEqualOrderByScheduledDateAscScheduledTimeAsc(
            UUID courseId, LiveClassStatus status, LocalDate onOrAfter);

    List<LiveClass> findByCourseIdInAndStatusAndScheduledDateGreaterThanEqualOrderByScheduledDateAscScheduledTimeAsc(
            List<UUID> courseIds, LiveClassStatus status, LocalDate onOrAfter, Pageable pageable);

    long countByScheduledDateBetween(LocalDate from, LocalDate to);

    long countByScheduledDateBetweenAndStatus(LocalDate from, LocalDate to, LiveClassStatus status);
}
