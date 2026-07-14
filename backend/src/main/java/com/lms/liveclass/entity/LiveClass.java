package com.lms.liveclass.entity;

import com.lms.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/** Ref: SRS Chapter 11 - Live Classes. Recurring schedules not supported in V1. */
@Getter
@Setter
@Entity
@Table(name = "live_classes")
public class LiveClass extends BaseEntity {

    @Column(name = "course_id", nullable = false, updatable = false)
    private UUID courseId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "scheduled_time", nullable = false)
    private LocalTime scheduledTime;

    @Column(name = "meeting_url", nullable = false)
    private String meetingUrl;

    @Column(name = "meeting_password")
    private String meetingPassword;

    // Ref: SRS 7.8, 11.11 - same private/unlisted, domain-restricted hosting requirement as lesson videos.
    @Column(name = "recording_url")
    private String recordingUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LiveClassStatus status = LiveClassStatus.SCHEDULED;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;
}
