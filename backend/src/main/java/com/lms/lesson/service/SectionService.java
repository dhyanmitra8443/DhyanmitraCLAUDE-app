package com.lms.lesson.service;

import com.lms.config.security.UserPrincipal;
import com.lms.course.service.CourseService;
import com.lms.lesson.dto.CreateSectionRequest;
import com.lms.lesson.dto.LessonSummaryResponse;
import com.lms.lesson.dto.SectionDetailResponse;
import com.lms.lesson.entity.ContentStatus;
import com.lms.lesson.entity.Lesson;
import com.lms.lesson.entity.Section;
import com.lms.lesson.repository.LessonRepository;
import com.lms.lesson.repository.SectionRepository;
import com.lms.shared.exception.BadRequestException;
import com.lms.shared.exception.BusinessRuleViolationException;
import com.lms.shared.exception.ConflictException;
import com.lms.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Ref: SRS Chapter 7 - Section & Lesson Management (sections). */
@Service
public class SectionService {

    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final CourseService courseService;

    public SectionService(SectionRepository sectionRepository, LessonRepository lessonRepository, CourseService courseService) {
        this.sectionRepository = sectionRepository;
        this.lessonRepository = lessonRepository;
        this.courseService = courseService;
    }

    @Transactional(readOnly = true)
    public List<SectionDetailResponse> getCourseOutline(UUID courseId, UserPrincipal principal) {
        // Ref: SRS 7.3, 7.9, 7.10 - students/public see only PUBLISHED
        // sections/lessons; admins and the course's assigned instructors see everything.
        boolean seeAll = courseService.isAdminOrAssignedInstructor(courseId, principal);

        List<Section> sections = sectionRepository.findByCourseIdOrderByDisplayOrderAsc(courseId);
        if (!seeAll) {
            sections = sections.stream().filter(s -> s.getStatus() == ContentStatus.PUBLISHED).toList();
        }

        return sections.stream().map(section -> toDetail(section, seeAll)).toList();
    }

    @Transactional
    public SectionDetailResponse createSection(UUID courseId, CreateSectionRequest request, UserPrincipal principal) {
        courseService.assertAdminOrAssignedInstructor(courseId, principal);
        if (sectionRepository.existsByCourseIdAndTitle(courseId, request.title())) {
            throw new ConflictException("A section with this title already exists in this course.");
        }

        Section section = new Section();
        section.setCourseId(courseId);
        applyRequest(section, request, courseId, null);
        return toDetail(sectionRepository.save(section), true);
    }

    @Transactional
    public void reorderSections(UUID courseId, List<UUID> sectionIdsInOrder, UserPrincipal principal) {
        courseService.assertAdminOrAssignedInstructor(courseId, principal);

        List<Section> sections = sectionRepository.findAllById(sectionIdsInOrder);
        if (sections.size() != sectionIdsInOrder.size() || sections.stream().anyMatch(s -> !s.getCourseId().equals(courseId))) {
            throw new BadRequestException("sectionIdsInOrder must reference only sections belonging to this course.");
        }
        if (sectionRepository.countByCourseId(courseId) != sectionIdsInOrder.size()) {
            throw new BadRequestException("sectionIdsInOrder must include every section in this course.");
        }

        Map<UUID, Section> byId = sections.stream().collect(Collectors.toMap(Section::getId, s -> s));

        // Two-phase: Hibernate emits one UPDATE per entity reflecting its
        // FINAL in-memory state at flush time, not one per setter call, so
        // writing straight to final values would still send them in
        // whatever order the persistence context picks and trip the
        // (course_id, display_order) unique constraint the moment two
        // rows' final positions cross. Temporary negative values, flushed,
        // then the real values, sidesteps that.
        int i = 1;
        for (UUID id : sectionIdsInOrder) {
            byId.get(id).setDisplayOrder(-i++);
        }
        sectionRepository.saveAllAndFlush(sections);

        i = 1;
        for (UUID id : sectionIdsInOrder) {
            byId.get(id).setDisplayOrder(i++);
        }
        sectionRepository.saveAllAndFlush(sections);
    }

    @Transactional
    public SectionDetailResponse updateSection(UUID sectionId, CreateSectionRequest request, UserPrincipal principal) {
        Section section = findSectionOrThrow(sectionId);
        courseService.assertAdminOrAssignedInstructor(section.getCourseId(), principal);

        if (sectionRepository.existsByCourseIdAndTitleAndIdNot(section.getCourseId(), request.title(), sectionId)) {
            throw new ConflictException("A section with this title already exists in this course.");
        }
        applyRequest(section, request, section.getCourseId(), section.getDisplayOrder());
        return toDetail(sectionRepository.save(section), true);
    }

    @Transactional
    public void publishSection(UUID sectionId, UserPrincipal principal) {
        Section section = findSectionOrThrow(sectionId);
        courseService.assertAdminOrAssignedInstructor(section.getCourseId(), principal);

        // Ref: SRS 7.4 - "Requires at least one published lesson."
        if (!lessonRepository.existsBySectionIdAndStatus(sectionId, ContentStatus.PUBLISHED)) {
            throw new BusinessRuleViolationException("Section cannot be published: it has no published lessons.");
        }
        section.setStatus(ContentStatus.PUBLISHED);
        sectionRepository.save(section);
    }

    @Transactional
    public void archiveSection(UUID sectionId, UserPrincipal principal) {
        Section section = findSectionOrThrow(sectionId);
        courseService.assertAdminOrAssignedInstructor(section.getCourseId(), principal);
        section.setStatus(ContentStatus.ARCHIVED);
        sectionRepository.save(section);
    }

    private void applyRequest(Section section, CreateSectionRequest request, UUID courseId, Integer currentDisplayOrder) {
        section.setTitle(request.title());
        section.setShortDescription(request.shortDescription());
        section.setDisplayOrder(request.displayOrder() != null
                ? request.displayOrder()
                : (currentDisplayOrder != null ? currentDisplayOrder : (int) sectionRepository.countByCourseId(courseId) + 1));
    }

    private SectionDetailResponse toDetail(Section section, boolean includeAllLessons) {
        List<Lesson> lessons = lessonRepository.findBySectionIdOrderByDisplayOrderAsc(section.getId());
        if (!includeAllLessons) {
            lessons = lessons.stream().filter(l -> l.getStatus() == ContentStatus.PUBLISHED).toList();
        }
        return new SectionDetailResponse(
                section.getId(),
                section.getCourseId(),
                section.getTitle(),
                section.getShortDescription(),
                section.getDisplayOrder(),
                section.getStatus().name(),
                lessons.stream().map(this::toLessonSummary).toList()
        );
    }

    private LessonSummaryResponse toLessonSummary(Lesson lesson) {
        return new LessonSummaryResponse(
                lesson.getId(),
                lesson.getSectionId(),
                lesson.getTitle(),
                lesson.getShortDescription(),
                lesson.getVideoDurationSeconds(),
                lesson.getThumbnailUrl(),
                lesson.getDisplayOrder(),
                lesson.getStatus().name(),
                lesson.isPreview(),
                null // Ref: SRS Ch.12 - progress tracking doesn't exist yet
        );
    }

    private Section findSectionOrThrow(UUID sectionId) {
        return sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found."));
    }
}
