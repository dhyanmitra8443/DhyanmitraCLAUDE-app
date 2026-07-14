package com.lms.resource.entity;

import com.lms.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Ref: SRS Chapter 8 - Lesson Resources & File Management.
 * externalUrl backs VIDEO/EXTERNAL_LINK; fileReference (an opaque private
 * storage reference from FileStorageService.writePrivate) backs
 * PDF/IMAGE/AUDIO/ZIP - mirrors the DB's lesson_resources_url_or_file CHECK
 * constraint, enforced again at the service layer for a clearer error
 * message than a raw constraint violation would give.
 */
@Getter
@Setter
@Entity
@Table(name = "lesson_resources")
public class LessonResource extends BaseEntity {

    @Column(name = "lesson_id", nullable = false, updatable = false)
    private UUID lessonId;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false)
    private ResourceType resourceType;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    private String description;

    @Column(name = "external_url")
    private String externalUrl;

    @Column(name = "file_reference")
    private String fileReference;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceStatus status = ResourceStatus.ACTIVE;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;
}
