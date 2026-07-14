package com.lms.dashboard.service;

import com.lms.category.repository.CategoryRepository;
import com.lms.certificate.dto.CertificateSummaryResponse;
import com.lms.certificate.repository.CertificateRepository;
import com.lms.certificate.service.CertificateService;
import com.lms.course.entity.Course;
import com.lms.course.entity.CourseStatus;
import com.lms.course.repository.CourseRepository;
import com.lms.course.service.CourseService;
import com.lms.dashboard.dto.AdminDashboardResponse;
import com.lms.dashboard.dto.ContinueLearningResponse;
import com.lms.dashboard.dto.InstructorCourseSummaryResponse;
import com.lms.dashboard.dto.InstructorDashboardResponse;
import com.lms.dashboard.dto.StudentCourseSummaryResponse;
import com.lms.dashboard.dto.StudentDashboardResponse;
import com.lms.lesson.entity.ContentStatus;
import com.lms.lesson.entity.Lesson;
import com.lms.lesson.repository.LessonRepository;
import com.lms.liveclass.dto.LiveClassSummaryResponse;
import com.lms.liveclass.entity.LiveClass;
import com.lms.liveclass.entity.LiveClassStatus;
import com.lms.liveclass.repository.LiveClassRepository;
import com.lms.payment.entity.PaymentStatus;
import com.lms.payment.repository.OrderRepository;
import com.lms.payment.repository.PaymentRepository;
import com.lms.progress.entity.ProgressStatus;
import com.lms.progress.repository.CourseCompletionRepository;
import com.lms.progress.repository.LessonProgressRepository;
import com.lms.progress.service.ProgressService;
import com.lms.subscription.entity.Subscription;
import com.lms.subscription.entity.SubscriptionStatus;
import com.lms.subscription.repository.SubscriptionPlanRepository;
import com.lms.subscription.repository.SubscriptionRepository;
import com.lms.user.entity.UserRole;
import com.lms.user.entity.UserStatus;
import com.lms.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Ref: SRS Chapter 13 - Dashboard & Analytics. Pure read-side aggregation
 * over everything built in Chapters 3-12; no new tables of its own.
 *
 * recentActivities is returned empty everywhere: openapi.yaml's
 * RecentActivity schema implies an audit trail, but no AuditService ever
 * writes to audit_logs anywhere in this codebase (Ref: SRS's own
 * cross-cutting mention of it, never backed by an implementation) - this
 * returns an honest empty list rather than inventing activity data.
 */
@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseService courseService;
    private final CategoryRepository categoryRepository;
    private final LiveClassRepository liveClassRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CertificateRepository certificateRepository;
    private final CertificateService certificateService;
    private final CourseCompletionRepository courseCompletionRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final LessonRepository lessonRepository;
    private final ProgressService progressService;

    public DashboardService(
            UserRepository userRepository,
            CourseRepository courseRepository,
            CourseService courseService,
            CategoryRepository categoryRepository,
            LiveClassRepository liveClassRepository,
            SubscriptionPlanRepository subscriptionPlanRepository,
            SubscriptionRepository subscriptionRepository,
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            CertificateRepository certificateRepository,
            CertificateService certificateService,
            CourseCompletionRepository courseCompletionRepository,
            LessonProgressRepository lessonProgressRepository,
            LessonRepository lessonRepository,
            ProgressService progressService
    ) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.courseService = courseService;
        this.categoryRepository = categoryRepository;
        this.liveClassRepository = liveClassRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.certificateRepository = certificateRepository;
        this.certificateService = certificateService;
        this.courseCompletionRepository = courseCompletionRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.lessonRepository = lessonRepository;
        this.progressService = progressService;
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getAdminDashboard() {
        BigDecimal totalRevenue = paymentRepository.sumAmountByStatus(PaymentStatus.SUCCESS);
        return new AdminDashboardResponse(
                userRepository.countByRole(UserRole.STUDENT),
                userRepository.countByRoleAndStatus(UserRole.STUDENT, UserStatus.ACTIVE),
                userRepository.countByRole(UserRole.INSTRUCTOR),
                courseRepository.count(),
                courseRepository.countByStatus(CourseStatus.PUBLISHED),
                courseRepository.countByStatus(CourseStatus.DRAFT),
                courseRepository.countByStatus(CourseStatus.ARCHIVED),
                categoryRepository.count(),
                liveClassRepository.count(),
                liveClassRepository.countByStatus(LiveClassStatus.SCHEDULED),
                liveClassRepository.countByStatus(LiveClassStatus.COMPLETED),
                subscriptionPlanRepository.count(),
                subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE),
                subscriptionRepository.countByStatus(SubscriptionStatus.EXPIRED),
                orderRepository.count(),
                paymentRepository.countByStatus(PaymentStatus.SUCCESS),
                paymentRepository.countByStatus(PaymentStatus.FAILED),
                totalRevenue == null ? BigDecimal.ZERO : totalRevenue,
                certificateRepository.count(),
                List.of()
        );
    }

    @Transactional(readOnly = true)
    public InstructorDashboardResponse getInstructorDashboard(UUID instructorId) {
        List<Course> assignedCourses = courseRepository.findByInstructors_Id(instructorId);
        List<UUID> courseIds = assignedCourses.stream().map(Course::getId).toList();
        LocalDate today = LocalDate.now();

        long publishedCourses = assignedCourses.stream().filter(c -> c.getStatus() == CourseStatus.PUBLISHED).count();
        long draftCourses = assignedCourses.stream().filter(c -> c.getStatus() == CourseStatus.DRAFT).count();

        List<InstructorCourseSummaryResponse> courseSummaries = assignedCourses.stream()
                .map(course -> new InstructorCourseSummaryResponse(
                        course.getId(),
                        course.getTitle(),
                        subscriptionRepository.countByCourseId(course.getId()),
                        subscriptionRepository.countByCourseIdAndStatus(course.getId(), SubscriptionStatus.ACTIVE),
                        nextLiveClassAt(course.getId(), today),
                        course.getStatus().name()
                ))
                .toList();

        return new InstructorDashboardResponse(
                assignedCourses.size(),
                publishedCourses,
                draftCourses,
                subscriptionRepository.countByCourseIdIn(courseIds),
                liveClassRepository.countByCourseIdInAndStatusAndScheduledDateGreaterThanEqual(courseIds, LiveClassStatus.SCHEDULED, today),
                liveClassRepository.countByCourseIdInAndStatus(courseIds, LiveClassStatus.COMPLETED),
                certificateRepository.countByCourseIdIn(courseIds),
                courseSummaries,
                List.of()
        );
    }

    @Transactional(readOnly = true)
    public StudentDashboardResponse getStudentDashboard(UUID studentId) {
        List<Subscription> subscriptions = subscriptionRepository.findByStudentId(studentId);
        List<Subscription> activeSubscriptions = subscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE).toList();
        List<UUID> activeCourseIds = activeSubscriptions.stream().map(Subscription::getCourseId).toList();
        LocalDate today = LocalDate.now();

        double overallLearningProgress = activeCourseIds.isEmpty() ? 0.0
                : activeCourseIds.stream()
                        .mapToDouble(courseId -> progressService.getOwnProgress(courseId, studentId).progressPercentage())
                        .average()
                        .orElse(0.0);

        List<LiveClassSummaryResponse> upcomingLiveClasses = liveClassRepository
                .findByCourseIdInAndStatusAndScheduledDateGreaterThanEqualOrderByScheduledDateAscScheduledTimeAsc(
                        activeCourseIds, LiveClassStatus.SCHEDULED, today, PageRequest.of(0, 10))
                .stream()
                .map(this::toLiveClassSummary)
                .toList();

        List<StudentCourseSummaryResponse> myCourses = activeSubscriptions.stream()
                .map(sub -> {
                    double progressPercentage = progressService.getOwnProgress(sub.getCourseId(), studentId).progressPercentage();
                    return new StudentCourseSummaryResponse(courseService.getCourseSummary(sub.getCourseId()), progressPercentage, sub.getEndDate());
                })
                .toList();

        List<CertificateSummaryResponse> certificates = certificateService.getOwnCertificates(studentId);

        return new StudentDashboardResponse(
                activeCourseIds.size(),
                courseCompletionRepository.findByStudentId(studentId).size(),
                overallLearningProgress,
                upcomingLiveClasses,
                continueLearning(studentId, activeCourseIds),
                myCourses,
                certificates,
                List.of()
        );
    }

    private ContinueLearningResponse continueLearning(UUID studentId, List<UUID> activeCourseIds) {
        Optional<UUID> inProgressCourseId = activeCourseIds.stream()
                .filter(courseId -> courseCompletionRepository.findByStudentIdAndCourseId(studentId, courseId).isEmpty())
                .findFirst();
        if (inProgressCourseId.isEmpty()) {
            return null;
        }
        UUID courseId = inProgressCourseId.get();

        List<Lesson> publishedLessons = lessonRepository.findByCourseIdAndStatus(courseId, ContentStatus.PUBLISHED).stream()
                .sorted(Comparator.comparing(Lesson::getDisplayOrder))
                .toList();
        if (publishedLessons.isEmpty()) {
            return new ContinueLearningResponse(courseId, null, null);
        }

        List<UUID> lessonIds = publishedLessons.stream().map(Lesson::getId).toList();
        var progressRows = lessonProgressRepository.findByStudentIdAndLessonIdIn(studentId, lessonIds);

        UUID lastCompletedLessonId = progressRows.stream()
                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED)
                .max(Comparator.comparing(p -> p.getCompletedAt() == null ? OffsetDateTime.MIN : p.getCompletedAt()))
                .map(p -> p.getLessonId())
                .orElse(null);

        var completedLessonIds = progressRows.stream()
                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED)
                .map(p -> p.getLessonId())
                .toList();
        UUID nextLessonId = publishedLessons.stream()
                .map(Lesson::getId)
                .filter(id -> !completedLessonIds.contains(id))
                .findFirst()
                .orElse(null);

        return new ContinueLearningResponse(courseId, lastCompletedLessonId, nextLessonId);
    }

    private OffsetDateTime nextLiveClassAt(UUID courseId, LocalDate today) {
        return liveClassRepository
                .findFirstByCourseIdAndStatusAndScheduledDateGreaterThanEqualOrderByScheduledDateAscScheduledTimeAsc(
                        courseId, LiveClassStatus.SCHEDULED, today)
                .map(lc -> LocalDateTime.of(lc.getScheduledDate(), lc.getScheduledTime()).atZone(ZoneId.systemDefault()).toOffsetDateTime())
                .orElse(null);
    }

    private LiveClassSummaryResponse toLiveClassSummary(LiveClass liveClass) {
        return new LiveClassSummaryResponse(
                liveClass.getId(),
                liveClass.getCourseId(),
                liveClass.getTitle(),
                liveClass.getDescription(),
                liveClass.getScheduledDate(),
                liveClass.getScheduledTime(),
                liveClass.getMeetingUrl(),
                liveClass.getRecordingUrl(),
                liveClass.getStatus().name()
        );
    }
}
