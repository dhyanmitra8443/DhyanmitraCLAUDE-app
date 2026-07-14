package com.lms.dashboard.service;

import com.lms.category.repository.CategoryRepository;
import com.lms.certificate.repository.CertificateRepository;
import com.lms.course.entity.CourseStatus;
import com.lms.course.repository.CourseRepository;
import com.lms.liveclass.entity.LiveClassStatus;
import com.lms.liveclass.repository.LiveClassRepository;
import com.lms.payment.entity.PaymentStatus;
import com.lms.payment.repository.OrderRepository;
import com.lms.payment.repository.PaymentRepository;
import com.lms.progress.entity.ProgressStatus;
import com.lms.progress.repository.CourseCompletionRepository;
import com.lms.progress.repository.LessonProgressRepository;
import com.lms.subscription.entity.SubscriptionStatus;
import com.lms.subscription.repository.SubscriptionPlanRepository;
import com.lms.subscription.repository.SubscriptionRepository;
import com.lms.user.entity.UserRole;
import com.lms.user.entity.UserStatus;
import com.lms.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ref: SRS 13.9 - five named analytics summaries, each shaped freely
 * (openapi.yaml declares `data: object` with no fixed schema for this
 * endpoint). dateFrom/dateTo are optional; when omitted, range-scoped
 * figures fall back to all-time.
 */
@Service
public class AnalyticsService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final LiveClassRepository liveClassRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CertificateRepository certificateRepository;
    private final CourseCompletionRepository courseCompletionRepository;
    private final LessonProgressRepository lessonProgressRepository;

    public AnalyticsService(
            UserRepository userRepository,
            CourseRepository courseRepository,
            CategoryRepository categoryRepository,
            LiveClassRepository liveClassRepository,
            SubscriptionPlanRepository subscriptionPlanRepository,
            SubscriptionRepository subscriptionRepository,
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            CertificateRepository certificateRepository,
            CourseCompletionRepository courseCompletionRepository,
            LessonProgressRepository lessonProgressRepository
    ) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.liveClassRepository = liveClassRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.certificateRepository = certificateRepository;
        this.courseCompletionRepository = courseCompletionRepository;
        this.lessonProgressRepository = lessonProgressRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAnalytics(AnalyticsType type, LocalDate dateFrom, LocalDate dateTo) {
        OffsetDateTime from = startOfDay(dateFrom, LocalDate.of(1970, 1, 1));
        OffsetDateTime to = endOfDay(dateTo, LocalDate.now());

        return switch (type) {
            case STUDENTS -> studentsAnalytics(from, to);
            case COURSES -> coursesAnalytics(from, to);
            case REVENUE -> revenueAnalytics(from, to);
            case LEARNING -> learningAnalytics(from, to);
            case LIVE_CLASSES -> liveClassesAnalytics(dateFrom == null ? LocalDate.of(1970, 1, 1) : dateFrom, dateTo == null ? LocalDate.now() : dateTo);
        };
    }

    private Map<String, Object> studentsAnalytics(OffsetDateTime from, OffsetDateTime to) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalStudents", userRepository.countByRole(UserRole.STUDENT));
        data.put("activeStudents", userRepository.countByRoleAndStatus(UserRole.STUDENT, UserStatus.ACTIVE));
        data.put("inactiveStudents", userRepository.countByRoleAndStatus(UserRole.STUDENT, UserStatus.INACTIVE));
        data.put("blockedStudents", userRepository.countByRoleAndStatus(UserRole.STUDENT, UserStatus.BLOCKED));
        data.put("newStudentsInRange", userRepository.countByRoleAndCreatedAtBetween(UserRole.STUDENT, from, to));
        data.put("totalInstructors", userRepository.countByRole(UserRole.INSTRUCTOR));
        return data;
    }

    private Map<String, Object> coursesAnalytics(OffsetDateTime from, OffsetDateTime to) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalCourses", courseRepository.count());
        data.put("publishedCourses", courseRepository.countByStatus(CourseStatus.PUBLISHED));
        data.put("draftCourses", courseRepository.countByStatus(CourseStatus.DRAFT));
        data.put("archivedCourses", courseRepository.countByStatus(CourseStatus.ARCHIVED));
        data.put("coursesCreatedInRange", courseRepository.countByCreatedAtBetween(from, to));
        data.put("totalCategories", categoryRepository.count());
        data.put("totalSubscriptionPlans", subscriptionPlanRepository.count());
        return data;
    }

    private Map<String, Object> revenueAnalytics(OffsetDateTime from, OffsetDateTime to) {
        BigDecimal totalRevenue = paymentRepository.sumAmountByStatus(PaymentStatus.SUCCESS);
        BigDecimal revenueInRange = paymentRepository.sumAmountByStatusAndPaymentDateBetween(PaymentStatus.SUCCESS, from, to);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalRevenue", totalRevenue == null ? BigDecimal.ZERO : totalRevenue);
        data.put("revenueInRange", revenueInRange == null ? BigDecimal.ZERO : revenueInRange);
        data.put("successfulPayments", paymentRepository.countByStatus(PaymentStatus.SUCCESS));
        data.put("failedPayments", paymentRepository.countByStatus(PaymentStatus.FAILED));
        data.put("successfulPaymentsInRange", paymentRepository.countByStatusAndPaymentDateBetween(PaymentStatus.SUCCESS, from, to));
        data.put("totalOrders", orderRepository.count());
        data.put("ordersInRange", orderRepository.countByCreatedAtBetween(from, to));
        data.put("activeSubscriptions", subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE));
        data.put("expiredSubscriptions", subscriptionRepository.countByStatus(SubscriptionStatus.EXPIRED));
        return data;
    }

    private Map<String, Object> learningAnalytics(OffsetDateTime from, OffsetDateTime to) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalLessonCompletions", lessonProgressRepository.countByStatus(ProgressStatus.COMPLETED));
        data.put("lessonCompletionsInRange", lessonProgressRepository.countByStatusAndCompletedAtBetween(ProgressStatus.COMPLETED, from, to));
        data.put("totalCourseCompletions", courseCompletionRepository.count());
        data.put("courseCompletionsInRange", courseCompletionRepository.countByCompletedAtBetween(from, to));
        data.put("certificatesIssued", certificateRepository.count());
        data.put("certificatesIssuedInRange", certificateRepository.countByCreatedAtBetween(from, to));
        return data;
    }

    private Map<String, Object> liveClassesAnalytics(LocalDate from, LocalDate to) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalLiveClasses", liveClassRepository.count());
        data.put("scheduledLiveClasses", liveClassRepository.countByStatus(LiveClassStatus.SCHEDULED));
        data.put("completedLiveClasses", liveClassRepository.countByStatus(LiveClassStatus.COMPLETED));
        data.put("cancelledLiveClasses", liveClassRepository.countByStatus(LiveClassStatus.CANCELLED));
        data.put("liveClassesInRange", liveClassRepository.countByScheduledDateBetween(from, to));
        data.put("completedLiveClassesInRange", liveClassRepository.countByScheduledDateBetweenAndStatus(from, to, LiveClassStatus.COMPLETED));
        return data;
    }

    private OffsetDateTime startOfDay(LocalDate date, LocalDate fallback) {
        return (date == null ? fallback : date).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
    }

    private OffsetDateTime endOfDay(LocalDate date, LocalDate fallback) {
        return (date == null ? fallback : date).plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
    }
}
