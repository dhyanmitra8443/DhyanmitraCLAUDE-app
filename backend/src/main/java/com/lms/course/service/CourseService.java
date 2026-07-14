package com.lms.course.service;

import com.lms.auth.dto.UserSummary;
import com.lms.category.dto.CategorySummaryResponse;
import com.lms.category.entity.Category;
import com.lms.category.repository.CategoryRepository;
import com.lms.config.security.UserPrincipal;
import com.lms.course.dto.CourseDetailResponse;
import com.lms.course.dto.CourseSummaryResponse;
import com.lms.course.dto.CreateCourseRequest;
import com.lms.course.entity.Course;
import com.lms.course.entity.CourseStatus;
import com.lms.course.entity.DifficultyLevel;
import com.lms.course.repository.CourseRepository;
import com.lms.course.repository.CourseSpecifications;
import com.lms.lesson.entity.ContentStatus;
import com.lms.lesson.repository.LessonRepository;
import com.lms.shared.exception.BadRequestException;
import com.lms.shared.exception.BusinessRuleViolationException;
import com.lms.shared.exception.ConflictException;
import com.lms.shared.exception.ForbiddenException;
import com.lms.shared.exception.ResourceNotFoundException;
import com.lms.shared.response.PageResponse;
import com.lms.subscription.entity.PlanStatus;
import com.lms.subscription.repository.SubscriptionPlanRepository;
import com.lms.user.entity.User;
import com.lms.user.entity.UserRole;
import com.lms.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Ref: SRS Chapter 5 - Course Management. */
@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LessonRepository lessonRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public CourseService(
            CourseRepository courseRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            LessonRepository lessonRepository,
            SubscriptionPlanRepository subscriptionPlanRepository
    ) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.lessonRepository = lessonRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }

    /** Reused by SectionService/LessonService so Ch.7 content management follows the same ownership rule as Ch.5. */
    @Transactional(readOnly = true)
    public void assertAdminOrAssignedInstructor(UUID courseId, UserPrincipal principal) {
        requireAdminOrAssignedInstructor(findCourseOrThrow(courseId), principal);
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseSummaryResponse> listCourses(
            String search, List<UUID> categoryIds, UUID instructorId, DifficultyLevel difficultyLevel,
            String language, CourseStatus status, boolean isAdmin, Pageable pageable
    ) {
        // Ref: SRS 5.9 - only administrators may see non-PUBLISHED courses;
        // everyone else (public/student/instructor) is forced to PUBLISHED.
        CourseStatus effectiveStatus = isAdmin ? status : CourseStatus.PUBLISHED;

        Specification<Course> spec = Specification.where(CourseSpecifications.titleContains(search))
                .and(CourseSpecifications.inCategories(categoryIds))
                .and(CourseSpecifications.hasInstructor(instructorId))
                .and(CourseSpecifications.hasDifficulty(difficultyLevel))
                .and(CourseSpecifications.hasLanguage(language))
                .and(CourseSpecifications.hasStatus(effectiveStatus));

        Page<Course> page = courseRepository.findAll(spec, pageable);
        return PageResponse.from(page, this::toSummary);
    }

    /** Reused by SubscriptionService/Ch.10 orders to embed a CourseSummary without duplicating this mapping. */
    @Transactional(readOnly = true)
    public CourseSummaryResponse getCourseSummary(UUID courseId) {
        return toSummary(findCourseOrThrow(courseId));
    }

    @Transactional(readOnly = true)
    public CourseDetailResponse getCourseDetail(UUID courseId) {
        return toDetail(findCourseOrThrow(courseId));
    }

    @Transactional
    public CourseDetailResponse createCourse(CreateCourseRequest request) {
        if (courseRepository.existsByTitle(request.title())) {
            throw new ConflictException("A course with this title already exists.");
        }

        Course course = new Course();
        applyRequest(course, request);
        course.setStatus(CourseStatus.DRAFT);
        return toDetail(courseRepository.save(course));
    }

    @Transactional
    public CourseDetailResponse updateCourse(UUID courseId, CreateCourseRequest request, UserPrincipal principal) {
        Course course = findCourseOrThrow(courseId);
        requireAdminOrAssignedInstructor(course, principal);

        if (courseRepository.existsByTitleAndIdNot(request.title(), courseId)) {
            throw new ConflictException("A course with this title already exists.");
        }

        applyRequest(course, request);
        return toDetail(courseRepository.save(course));
    }

    @Transactional
    public void publishCourse(UUID courseId, UserPrincipal principal) {
        Course course = findCourseOrThrow(courseId);
        requireAdminOrAssignedInstructor(course, principal);

        // Ref: SRS 5.4 - "Requires at least one section/lesson, one category,
        // one instructor, and one ACTIVE subscription plan."
        List<String> failures = new ArrayList<>();
        if (course.getCategories().isEmpty()) failures.add("at least one category");
        if (course.getInstructors().isEmpty()) failures.add("at least one instructor");
        if (!lessonRepository.existsByCourseIdAndStatus(courseId, ContentStatus.PUBLISHED)) {
            failures.add("at least one published lesson");
        }
        if (!subscriptionPlanRepository.existsByCourseIdAndStatus(courseId, PlanStatus.ACTIVE)) {
            failures.add("at least one ACTIVE subscription plan");
        }
        if (!failures.isEmpty()) {
            throw new BusinessRuleViolationException("Course cannot be published: missing " + String.join(", ", failures) + ".");
        }

        course.setStatus(CourseStatus.PUBLISHED);
        course.setPublishedAt(OffsetDateTime.now());
        courseRepository.save(course);
    }

    @Transactional
    public void archiveCourse(UUID courseId, UserPrincipal principal) {
        Course course = findCourseOrThrow(courseId);
        requireAdminOrAssignedInstructor(course, principal);
        course.setStatus(CourseStatus.ARCHIVED);
        courseRepository.save(course);
    }

    @Transactional
    public void assignInstructor(UUID courseId, UUID instructorId) {
        Course course = findCourseOrThrow(courseId);
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found."));
        if (instructor.getRole() != UserRole.INSTRUCTOR) {
            throw new BadRequestException("User is not an instructor.");
        }
        course.getInstructors().add(instructor);
        courseRepository.save(course);
    }

    @Transactional
    public void removeInstructor(UUID courseId, UUID instructorId) {
        Course course = findCourseOrThrow(courseId);
        boolean wasPresent = course.getInstructors().removeIf(i -> i.getId().equals(instructorId));
        if (wasPresent && course.getStatus() == CourseStatus.PUBLISHED && course.getInstructors().isEmpty()) {
            throw new BusinessRuleViolationException("Cannot remove the last instructor from a published course.");
        }
        courseRepository.save(course);
    }

    @Transactional
    public void assignCategories(UUID courseId, Set<UUID> categoryIds, UserPrincipal principal) {
        Course course = findCourseOrThrow(courseId);
        requireAdminOrAssignedInstructor(course, principal);
        course.getCategories().addAll(findCategoriesOrThrow(categoryIds));
        courseRepository.save(course);
    }

    @Transactional
    public void removeCategory(UUID courseId, UUID categoryId, UserPrincipal principal) {
        Course course = findCourseOrThrow(courseId);
        requireAdminOrAssignedInstructor(course, principal);
        boolean wasPresent = course.getCategories().removeIf(c -> c.getId().equals(categoryId));
        if (wasPresent && course.getStatus() == CourseStatus.PUBLISHED && course.getCategories().isEmpty()) {
            throw new BusinessRuleViolationException("Cannot remove the last category from a published course.");
        }
        courseRepository.save(course);
    }

    private void applyRequest(Course course, CreateCourseRequest request) {
        course.setTitle(request.title());
        course.setShortDescription(request.shortDescription());
        course.setDetailedDescription(request.detailedDescription());
        course.setThumbnailUrl(request.thumbnailUrl());
        course.setLanguage(request.language());
        course.setDifficultyLevel(request.difficultyLevel());
        course.setEstimatedDurationMinutes(request.estimatedDurationMinutes());

        Set<User> instructors = findInstructorsOrThrow(request.instructorIds());
        course.setInstructors(instructors);

        course.setCategories(findCategoriesOrThrow(request.categoryIds()));
    }

    private Set<User> findInstructorsOrThrow(Set<UUID> instructorIds) {
        List<User> found = userRepository.findAllById(instructorIds);
        if (found.size() != instructorIds.size()) {
            throw new BadRequestException("One or more instructorIds do not exist.");
        }
        if (found.stream().anyMatch(u -> u.getRole() != UserRole.INSTRUCTOR)) {
            throw new BadRequestException("instructorIds must all reference INSTRUCTOR accounts.");
        }
        return new HashSet<>(found);
    }

    private Set<Category> findCategoriesOrThrow(Set<UUID> categoryIds) {
        List<Category> found = categoryRepository.findAllById(categoryIds);
        if (found.size() != categoryIds.size()) {
            throw new BadRequestException("One or more categoryIds do not exist.");
        }
        return new HashSet<>(found);
    }

    private void requireAdminOrAssignedInstructor(Course course, UserPrincipal principal) {
        if (!isAdminOrAssignedInstructor(course, principal)) {
            throw new ForbiddenException("You are not assigned to this course.");
        }
    }

    /**
     * Non-throwing variant for endpoints (e.g. the Ch.7 course outline) that
     * need to filter content by ownership rather than reject the whole
     * request - a public/student caller should just see less, not get a 403.
     */
    @Transactional(readOnly = true)
    public boolean isAdminOrAssignedInstructor(UUID courseId, UserPrincipal principal) {
        return isAdminOrAssignedInstructor(findCourseOrThrow(courseId), principal);
    }

    private boolean isAdminOrAssignedInstructor(Course course, UserPrincipal principal) {
        if (principal == null) {
            return false;
        }
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATOR"));
        if (isAdmin) {
            return true;
        }
        return course.getInstructors().stream().anyMatch(i -> i.getId().equals(principal.getUserId()));
    }

    private CourseSummaryResponse toSummary(Course course) {
        return new CourseSummaryResponse(
                course.getId(),
                course.getTitle(),
                course.getShortDescription(),
                course.getThumbnailUrl(),
                course.getLanguage(),
                course.getDifficultyLevel().name(),
                course.getEstimatedDurationMinutes(),
                course.getStatus().name(),
                course.getInstructors().stream().map(UserSummary::from).toList(),
                course.getCategories().stream().map(this::toCategorySummary).toList(),
                lessonCount(course.getId()),
                course.getPublishedAt()
        );
    }

    private CourseDetailResponse toDetail(Course course) {
        return new CourseDetailResponse(
                course.getId(),
                course.getTitle(),
                course.getShortDescription(),
                course.getThumbnailUrl(),
                course.getLanguage(),
                course.getDifficultyLevel().name(),
                course.getEstimatedDurationMinutes(),
                course.getStatus().name(),
                course.getInstructors().stream().map(UserSummary::from).toList(),
                course.getCategories().stream().map(this::toCategorySummary).toList(),
                lessonCount(course.getId()),
                course.getPublishedAt(),
                course.getDetailedDescription(),
                List.of(), // Ref: SRS Ch.9 - subscription plans don't exist yet
                course.getPreviewLessonId(),
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }

    // Ref: SRS Ch.5 CourseSummary/CourseDetail schema - "lessonCount" is
    // interpreted as PUBLISHED lesson count, since that's what a prospective
    // student browsing a published course actually cares about; a DRAFT
    // course being edited by its instructor would otherwise show a
    // misleadingly low count while content is still being built.
    private int lessonCount(UUID courseId) {
        return (int) lessonRepository.countByCourseIdAndStatus(courseId, ContentStatus.PUBLISHED);
    }

    private CategorySummaryResponse toCategorySummary(Category category) {
        return CategorySummaryResponse.from(category, courseRepository.countByCategories_Id(category.getId()));
    }

    private Course findCourseOrThrow(UUID courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found."));
    }
}
