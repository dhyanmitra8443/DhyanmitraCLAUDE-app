package com.lms.liveclass.repository;

import com.lms.liveclass.entity.LiveClassAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LiveClassAttendanceRepository extends JpaRepository<LiveClassAttendance, UUID> {

    Optional<LiveClassAttendance> findByLiveClassIdAndStudentId(UUID liveClassId, UUID studentId);

    /** Ref: SRS 15.4 - the student's own live-class attendance report. */
    List<LiveClassAttendance> findByStudentId(UUID studentId);

    /** Ref: SRS 15.5, 15.6 - attendee count shown per class in the live-class reports. */
    long countByLiveClassId(UUID liveClassId);
}
