package com.lms.lesson.service;

import com.lms.config.security.UserPrincipal;
import com.lms.course.service.CourseService;
import com.lms.lesson.dto.CreateLessonRequest;
import com.lms.lesson.dto.LessonDetailResponse;
import com.lms.lesson.entity.ContentStatus;
import com.lms.lesson.entity.Lesson;
import com.lms.lesson.entity.Section;
import com.lms.lesson.repository.LessonRepository;
import com.lms.lesson.repository.SectionRepository;
import com.lms.resource.entity.ResourceStatus;
import com.lms.resource.entity.ResourceType;
import com.lms.resource.repository.LessonResourceRepository;
import com.lms.shared.exception.BadRequestException;
import com.lms.shared.exception.BusinessRuleViolationException;
import com.lms.shared.exception.ConflictException;
import com.lms.shared.exception.ForbiddenException;
import com.lms.shared.exception.ResourceNotFoundException;
import com.lms.subscription.service.SubscriptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/** Ref: SRS Chapter 7 - Section & Lesson Management (lessons). */
@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final CourseService courseService;
    private final LessonResourceRepository lessonResourceRepository;
    private final SubscriptionService subscriptionService;

    public LessonService(
            LessonRepository lessonRepository,
            SectionRepository sectionRepository,
            CourseService courseService,
            LessonResourceRepository lessonResourceRepository,
            SubscriptionService subscriptionService
    ) {
        this.lessonRepository = lessonRepository;
        this.sectionRepository = sectionRepository;
        this.courseService = courseService;
        this.subscriptionService = subscriptionService;
        this.lessonResourceRepository = lessonResourceRepository;
    }

    @Transactional
    public LessonDetailResponse createLesson(UUID sectionId, CreateLessonRequest request, UserPrincipal principal) {
        Section section = findSectionOrThrow(sectionId);
        courseService.assertAdminOrAssignedInstructor(section.getCourseId(), principal);

        if (lessonRepository.existsBySectionIdAndTitle(sectionId, request.title())) {
            throw new ConflictException("A lesson with this title already exists in this section.");
        }

        Lesson lesson = new Lesson();
        lesson.setSectionId(sectionId);
        lesson.setCourseId(section.getCourseId());
        applyRequest(lesson, request, sectionId, null);
        return toDetail(lessonRepository.save(lesson));
    }

    @Transactional
    public void reorderLessons(UUID sectionId, List<UUID> lessonIdsInOrder, UserPrincipal principal) {
        Section section = findSectionOrThrow(sectionId);
        courseService.assertAdminOrAssignedInstructor(section.getCourseId(), principal);

        List<Lesson> lessons = lessonRepository.findAllById(lessonIdsInOrder);
        if (lessons.size() != lessonIdsInOrder.size() || lessons.stream().anyMatch(l -> !l.getSectionId().equals(sectionId))) {
            throw new BadRequestException("lessonIdsInOrder must reference only lessons belonging to this section.");
        }
        if (lessonRepository.countBySectionId(sectionId) != lessonIdsInOrder.size()) {
            throw new BadRequestException("lessonIdsInOrder must include every lesson in this section.");
        }

        Map<UUID, Lesson> byId = lessons.stream().collect(Collectors.toMap(Lesson::getId, l -> l));

        // Ref: SectionService.reorderSections - same two-phase reasoning
        // (Hibernate flushes one UPDATE per entity's final state, not per
        // setter call; temp negative values dodge the unique constraint).
        int i = 1;
        for (UUID id : lessonIdsInOrder) {
            byId.get(id).setDisplayOrder(-i++);
        }
        lessonRepository.saveAllAndFlush(lessons);

        i = 1;
        for (UUID id : lessonIdsInOrder) {
            byId.get(id).setDisplayOrder(i++);
        }
        lessonRepository.saveAllAndFlush(lessons);
    }

    @Transactional(readOnly = true)
    public LessonDetailResponse getLessonDetail(UUID lessonId, UserPrincipal principal) {
        Lesson lesson = findLessonOrThrow(lessonId);
        assertAccess(lesson, principal);
        return toDetail(lesson);
    }

    /**
     * Ref: SRS 7.14 - reused by LessonResourceService (Ch.8) for resource
     * listing/download, which shares this exact access rule.
     */
    @Transactional(readOnly = true)
    public void assertAccess(UUID lessonId, UserPrincipal principal) {
        assertAccess(findLessonOrThrow(lessonId), principal);
    }

    private void assertAccess(Lesson lesson, UserPrincipal principal) {
        if (courseService.isAdminOrAssignedInstructor(lesson.getCourseId(), principal)) {
            return;
        }
        if (lesson.getStatus() == ContentStatus.PUBLISHED && lesson.isPreview()) {
            return;
        }
        if (lesson.getStatus() == ContentStatus.PUBLISHED && principal != null
                && subscriptionService.hasActiveSubscription(principal.getUserId(), lesson.getCourseId())) {
            return;
        }
        throw new ForbiddenException("No active subscription, or lesson not published.");
    }

    @Transactional
    public LessonDetailResponse updateLesson(UUID lessonId, CreateLessonRequest request, UserPrincipal principal) {
        Lesson lesson = findLessonOrThrow(lessonId);
        courseService.assertAdminOrAssignedInstructor(lesson.getCourseId(), principal);

        if (lessonRepository.existsBySectionIdAndTitleAndIdNot(lesson.getSectionId(), request.title(), lessonId)) {
            throw new ConflictException("A lesson with this title already exists in this section.");
        }
        applyRequest(lesson, request, lesson.getSectionId(), lesson.getDisplayOrder());
        return toDetail(lessonRepository.save(lesson));
    }

    @Transactional
    public void publishLesson(UUID lessonId, UserPrincipal principal) {
        Lesson lesson = findLessonOrThrow(lessonId);
        courseService.assertAdminOrAssignedInstructor(lesson.getCourseId(), principal);

        // Ref: SRS 7.6, 8.3, 8.7 - "Requires an active VIDEO resource."
        if (!lessonResourceRepository.existsByLessonIdAndResourceTypeAndStatus(lessonId, ResourceType.VIDEO, ResourceStatus.ACTIVE)) {
            throw new BusinessRuleViolationException("Lesson cannot be published: no active VIDEO resource attached.");
        }
        lesson.setStatus(ContentStatus.PUBLISHED);
        lessonRepository.save(lesson);
    }

    @Transactional
    public void archiveLesson(UUID lessonId, UserPrincipal principal) {
        Lesson lesson = findLessonOrThrow(lessonId);
        courseService.assertAdminOrAssignedInstructor(lesson.getCourseId(), principal);
        lesson.setStatus(ContentStatus.ARCHIVED);
        lessonRepository.save(lesson);
    }

    @Transactional
    public void setPreview(UUID lessonId, boolean isPreview, UserPrincipal principal) {
        Lesson lesson = findLessonOrThrow(lessonId);
        courseService.assertAdminOrAssignedInstructor(lesson.getCourseId(), principal);

        if (isPreview) {
            // Ref: SRS 7.11 - "Exactly one lesson per course may be the
            // preview lesson; setting a new one automatically unsets the
            // previous one." The DB's partial unique index only allows one
            // is_preview=true row per course_id at a time, so the old one
            // must be unset and flushed before this one is set, same
            // reasoning as the reorder methods above.
            Optional<Lesson> currentPreview = lessonRepository.findByCourseIdAndIsPreviewTrue(lesson.getCourseId());
            if (currentPreview.isPresent() && !currentPreview.get().getId().equals(lessonId)) {
                currentPreview.get().setPreview(false);
                lessonRepository.saveAndFlush(currentPreview.get());
            }
        }
        lesson.setPreview(isPreview);
        lessonRepository.save(lesson);
    }

    private void applyRequest(Lesson lesson, CreateLessonRequest request, UUID sectionId, Integer currentDisplayOrder) {
        lesson.setTitle(request.title());
        lesson.setDetailedDescription(request.detailedDescription());
        lesson.setVideoUrl(request.videoUrl());
        lesson.setThumbnailUrl(request.thumbnailUrl());
        lesson.setDisplayOrder(request.displayOrder() != null
                ? request.displayOrder()
                : (currentDisplayOrder != null ? currentDisplayOrder : (int) lessonRepository.countBySectionId(sectionId) + 1));
    }

    private LessonDetailResponse toDetail(Lesson lesson) {
        return new LessonDetailResponse(
                lesson.getId(),
                lesson.getSectionId(),
                lesson.getTitle(),
                lesson.getShortDescription(),
                lesson.getVideoDurationSeconds(),
                lesson.getThumbnailUrl(),
                lesson.getDisplayOrder(),
                lesson.getStatus().name(),
                lesson.isPreview(),
                null, // Ref: SRS Ch.12 - progress tracking doesn't exist yet
                lesson.getDetailedDescription(),
                lesson.getVideoUrl(),
                List.of() // Ref: SRS Ch.8 - lesson resources don't exist yet
        );
    }

    private Section findSectionOrThrow(UUID sectionId) {
        return sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found."));
    }

    private Lesson findLessonOrThrow(UUID lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found."));
    }
}
