package com.lms.resource.service;

import com.lms.config.security.UserPrincipal;
import com.lms.course.service.CourseService;
import com.lms.lesson.entity.ContentStatus;
import com.lms.lesson.entity.Lesson;
import com.lms.lesson.repository.LessonRepository;
import com.lms.lesson.service.LessonService;
import com.lms.resource.dto.CreateLessonResourceRequest;
import com.lms.resource.dto.DownloadUrlResponse;
import com.lms.resource.dto.LessonResourceSummaryResponse;
import com.lms.resource.dto.UploadUrlRequest;
import com.lms.resource.dto.UploadUrlResponse;
import com.lms.resource.entity.LessonResource;
import com.lms.resource.entity.ResourceStatus;
import com.lms.resource.entity.ResourceType;
import com.lms.resource.repository.LessonResourceRepository;
import com.lms.shared.exception.BadRequestException;
import com.lms.shared.exception.BusinessRuleViolationException;
import com.lms.shared.exception.ConflictException;
import com.lms.shared.exception.ResourceNotFoundException;
import com.lms.shared.storage.FileStorageService;
import com.lms.shared.storage.SignedUrlService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Ref: SRS Chapter 8 - Lesson Resources & File Management. */
@Service
public class LessonResourceService {

    private static final Set<ResourceType> EXTERNAL_URL_TYPES = Set.of(ResourceType.VIDEO, ResourceType.EXTERNAL_LINK);
    private static final Set<ResourceType> FILE_REFERENCE_TYPES = Set.of(ResourceType.PDF, ResourceType.IMAGE, ResourceType.AUDIO, ResourceType.ZIP);
    private static final Duration UPLOAD_URL_TTL = Duration.ofMinutes(15);
    private static final Duration DOWNLOAD_URL_TTL = Duration.ofMinutes(5);

    private final LessonResourceRepository lessonResourceRepository;
    private final LessonRepository lessonRepository;
    private final LessonService lessonService;
    private final CourseService courseService;
    private final FileStorageService fileStorageService;
    private final SignedUrlService signedUrlService;

    public LessonResourceService(
            LessonResourceRepository lessonResourceRepository,
            LessonRepository lessonRepository,
            LessonService lessonService,
            CourseService courseService,
            FileStorageService fileStorageService,
            SignedUrlService signedUrlService
    ) {
        this.lessonResourceRepository = lessonResourceRepository;
        this.lessonRepository = lessonRepository;
        this.lessonService = lessonService;
        this.courseService = courseService;
        this.fileStorageService = fileStorageService;
        this.signedUrlService = signedUrlService;
    }

    @Transactional(readOnly = true)
    public List<LessonResourceSummaryResponse> listResources(UUID lessonId, UserPrincipal principal) {
        // Ref: SRS 8.4, 8.11 - shares GET /lessons/{lessonId}'s access rule.
        lessonService.assertAccess(lessonId, principal);
        Lesson lesson = findLessonOrThrow(lessonId);
        boolean fullAccess = courseService.isAdminOrAssignedInstructor(lesson.getCourseId(), principal);

        List<LessonResource> resources = lessonResourceRepository.findByLessonIdOrderByDisplayOrderAsc(lessonId);
        if (!fullAccess) {
            resources = resources.stream().filter(r -> r.getStatus() == ResourceStatus.ACTIVE).toList();
        }
        return resources.stream().map(this::toSummary).toList();
    }

    @Transactional
    public LessonResourceSummaryResponse createResource(UUID lessonId, CreateLessonResourceRequest request, UserPrincipal principal) {
        Lesson lesson = findLessonOrThrow(lessonId);
        courseService.assertAdminOrAssignedInstructor(lesson.getCourseId(), principal);

        LessonResource resource = new LessonResource();
        resource.setLessonId(lessonId);
        applyRequest(resource, request, lessonId, null);
        return toSummary(lessonResourceRepository.save(resource));
    }

    @Transactional(readOnly = true)
    public UploadUrlResponse issueUploadUrl(UploadUrlRequest request) {
        String extension = StringUtils.getFilenameExtension(request.fileName());
        String reference = "lesson-resources/" + UUID.randomUUID() + (extension != null ? "." + extension : "");
        String token = signedUrlService.issue(reference, UPLOAD_URL_TTL);
        return new UploadUrlResponse("/api/v1/lesson-resources/upload/" + token, reference, UPLOAD_URL_TTL.toSeconds());
    }

    public void consumeUpload(String token, MultipartFile file) {
        String reference = signedUrlService.validate(token)
                .orElseThrow(() -> new BadRequestException("Upload link is invalid or has expired."));
        fileStorageService.writePrivate(file, reference);
    }

    @Transactional
    public LessonResourceSummaryResponse updateResource(UUID resourceId, CreateLessonResourceRequest request, UserPrincipal principal) {
        LessonResource resource = findResourceOrThrow(resourceId);
        Lesson lesson = findLessonOrThrow(resource.getLessonId());
        courseService.assertAdminOrAssignedInstructor(lesson.getCourseId(), principal);

        applyRequest(resource, request, resource.getLessonId(), resource.getId());
        return toSummary(lessonResourceRepository.save(resource));
    }

    @Transactional
    public void archiveResource(UUID resourceId, UserPrincipal principal) {
        LessonResource resource = findResourceOrThrow(resourceId);
        Lesson lesson = findLessonOrThrow(resource.getLessonId());
        courseService.assertAdminOrAssignedInstructor(lesson.getCourseId(), principal);

        // Ref: SRS 8.14 - archiving the only active VIDEO resource of a
        // published lesson would silently break Ch.7's publish
        // prerequisite (an ACTIVE VIDEO resource must exist) without this guard.
        boolean isTheActiveVideo = resource.getResourceType() == ResourceType.VIDEO && resource.getStatus() == ResourceStatus.ACTIVE;
        if (isTheActiveVideo && lesson.getStatus() == ContentStatus.PUBLISHED) {
            long activeVideoCount = lessonResourceRepository.countByLessonIdAndResourceTypeAndStatus(
                    resource.getLessonId(), ResourceType.VIDEO, ResourceStatus.ACTIVE);
            if (activeVideoCount <= 1) {
                throw new BusinessRuleViolationException("Cannot archive the only active video resource of a published lesson.");
            }
        }

        resource.setStatus(ResourceStatus.ARCHIVED);
        lessonResourceRepository.save(resource);
    }

    @Transactional(readOnly = true)
    public DownloadUrlResponse issueDownloadUrl(UUID resourceId, UserPrincipal principal) {
        LessonResource resource = findResourceOrThrow(resourceId);
        // Ref: SRS 8.12 - shares GET /lessons/{lessonId}'s access rule.
        lessonService.assertAccess(resource.getLessonId(), principal);

        if (EXTERNAL_URL_TYPES.contains(resource.getResourceType())) {
            throw new BadRequestException("VIDEO/EXTERNAL_LINK resources are accessed via their externalUrl directly, not a download link.");
        }

        String token = signedUrlService.issue(resourceId.toString(), DOWNLOAD_URL_TTL);
        return new DownloadUrlResponse("/api/v1/lesson-resources/download/" + token, DOWNLOAD_URL_TTL.toSeconds());
    }

    @Transactional(readOnly = true)
    public DownloadPayload consumeDownload(String token) {
        UUID resourceId = signedUrlService.validate(token)
                .map(UUID::fromString)
                .orElseThrow(() -> new BadRequestException("Download link is invalid or has expired."));
        LessonResource resource = findResourceOrThrow(resourceId);
        Resource fileResource = fileStorageService.loadPrivate(resource.getFileReference());

        // The original uploaded filename only exists transiently (in the
        // MultipartFile at upload time, before this DB row exists) and
        // isn't part of the documented response schema, so rather than
        // thread it through extra state, derive a reasonable
        // filename/content-type from the resource's own extension and
        // display name at download time.
        String extension = StringUtils.getFilenameExtension(resource.getFileReference());
        String filename = resource.getDisplayName() + (extension != null ? "." + extension : "");
        String contentType = extension != null
                ? MediaTypeFactory.getMediaType("file." + extension).map(MediaType::toString).orElse(null)
                : null;
        return new DownloadPayload(fileResource, filename, contentType);
    }

    public record DownloadPayload(Resource resource, String fileName, String contentType) {
    }

    private void applyRequest(LessonResource resource, CreateLessonResourceRequest request, UUID lessonId, UUID currentResourceId) {
        ResourceType type = request.resourceType();
        resource.setResourceType(type);
        resource.setDisplayName(request.displayName());
        resource.setDescription(request.description());

        if (EXTERNAL_URL_TYPES.contains(type)) {
            if (request.externalUrl() == null || request.externalUrl().isBlank()) {
                throw new BadRequestException("externalUrl is required for VIDEO/EXTERNAL_LINK resources.");
            }
            resource.setExternalUrl(request.externalUrl());
            resource.setFileReference(null);
            resource.setFileSizeBytes(null);
        } else {
            if (request.fileReference() == null || request.fileReference().isBlank()) {
                throw new BadRequestException("fileReference is required for PDF/IMAGE/AUDIO/ZIP resources.");
            }
            if (!fileStorageService.existsPrivate(request.fileReference())) {
                throw new BadRequestException("No file has been uploaded for this fileReference.");
            }
            resource.setFileReference(request.fileReference());
            resource.setFileSizeBytes(fileStorageService.sizeOfPrivate(request.fileReference()));
            resource.setExternalUrl(null);
        }

        // Ref: SRS 8.6 - "Lesson already has an active VIDEO resource" 409;
        // checked here (not just left to the DB's partial unique index) for
        // a clearer message than a raw constraint violation would give.
        if (type == ResourceType.VIDEO) {
            boolean hasOtherActiveVideo = lessonResourceRepository.findByLessonIdOrderByDisplayOrderAsc(lessonId).stream()
                    .anyMatch(r -> r.getResourceType() == ResourceType.VIDEO
                            && r.getStatus() == ResourceStatus.ACTIVE
                            && !r.getId().equals(currentResourceId));
            if (hasOtherActiveVideo) {
                throw new ConflictException("Lesson already has an active VIDEO resource.");
            }
        }

        resource.setDisplayOrder(request.displayOrder() != null
                ? request.displayOrder()
                : (int) lessonResourceRepository.countByLessonId(lessonId) + 1);
    }

    private LessonResourceSummaryResponse toSummary(LessonResource resource) {
        return new LessonResourceSummaryResponse(
                resource.getId(),
                resource.getLessonId(),
                resource.getResourceType().name(),
                resource.getDisplayName(),
                resource.getDescription(),
                resource.getExternalUrl(),
                resource.getFileReference(),
                resource.getFileSizeBytes(),
                resource.getDisplayOrder(),
                resource.getStatus().name()
        );
    }

    private Lesson findLessonOrThrow(UUID lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found."));
    }

    private LessonResource findResourceOrThrow(UUID resourceId) {
        return lessonResourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson resource not found."));
    }
}
