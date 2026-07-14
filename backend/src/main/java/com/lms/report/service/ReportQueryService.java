package com.lms.report.service;

import com.lms.category.entity.Category;
import com.lms.category.repository.CategoryRepository;
import com.lms.certificate.entity.Certificate;
import com.lms.certificate.repository.CertificateRepository;
import com.lms.course.entity.Course;
import com.lms.course.repository.CourseRepository;
import com.lms.lesson.entity.ContentStatus;
import com.lms.lesson.entity.Lesson;
import com.lms.lesson.repository.LessonRepository;
import com.lms.liveclass.entity.LiveClass;
import com.lms.liveclass.entity.LiveClassAttendance;
import com.lms.liveclass.repository.LiveClassAttendanceRepository;
import com.lms.liveclass.repository.LiveClassRepository;
import com.lms.payment.entity.Order;
import com.lms.payment.entity.Payment;
import com.lms.payment.entity.PaymentStatus;
import com.lms.payment.repository.OrderRepository;
import com.lms.payment.repository.PaymentRepository;
import com.lms.progress.entity.ProgressStatus;
import com.lms.progress.repository.CourseCompletionRepository;
import com.lms.progress.repository.LessonProgressRepository;
import com.lms.report.ReportCriteria;
import com.lms.report.ReportKey;
import com.lms.subscription.entity.Subscription;
import com.lms.subscription.entity.SubscriptionPlan;
import com.lms.subscription.repository.SubscriptionPlanRepository;
import com.lms.subscription.repository.SubscriptionRepository;
import com.lms.user.entity.User;
import com.lms.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Ref: SRS Chapter 15 - Reports Management. Produces the row data for every
 * ReportKey; formatting into PDF/XLSX/CSV is ReportExportService's job.
 *
 * Rows are ordered maps keyed by the ReportKey's own column keys, so adding
 * a column is a one-line change in two places rather than a new DTO.
 *
 * Scoping (SRS 15.13) is applied at the source, never as a post-filter: a
 * student's reports read only rows belonging to that student, and an
 * instructor's read only their own courses. ReportService checks the role
 * owns the key before any of this runs.
 *
 * Known scale limit: rows are assembled in memory and then paginated, rather
 * than pushed down into SQL LIMIT/OFFSET. Several of these reports are
 * cross-entity aggregates (revenue by month, per-course learning analytics,
 * per-student progress) that cannot be expressed as a single paged entity
 * query anyway, so doing it uniformly keeps filtering and sorting consistent
 * across all seventeen. It is appropriate at the scale this LMS is specified
 * for (a single institute), and it is exactly why exports above
 * ReportExportService.ASYNC_ROW_THRESHOLD rows are pushed to a background
 * job. If a deployment ever outgrows this, the entity-backed reports should
 * move to Specification-based paged queries first.
 */
@Service
public class ReportQueryService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CertificateRepository certificateRepository;
    private final LiveClassRepository liveClassRepository;
    private final LiveClassAttendanceRepository liveClassAttendanceRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final CourseCompletionRepository courseCompletionRepository;

    public ReportQueryService(
            UserRepository userRepository,
            CourseRepository courseRepository,
            CategoryRepository categoryRepository,
            SubscriptionRepository subscriptionRepository,
            SubscriptionPlanRepository subscriptionPlanRepository,
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            CertificateRepository certificateRepository,
            LiveClassRepository liveClassRepository,
            LiveClassAttendanceRepository liveClassAttendanceRepository,
            LessonRepository lessonRepository,
            LessonProgressRepository lessonProgressRepository,
            CourseCompletionRepository courseCompletionRepository
    ) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.certificateRepository = certificateRepository;
        this.liveClassRepository = liveClassRepository;
        this.liveClassAttendanceRepository = liveClassAttendanceRepository;
        this.lessonRepository = lessonRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.courseCompletionRepository = courseCompletionRepository;
    }

    /** All rows for a report, with every filter and scoping rule applied. */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> fetchRows(ReportKey key, ReportCriteria criteria) {
        List<Map<String, Object>> rows = switch (key) {
            case STUDENT_MY_COURSES -> studentMyCourses(criteria);
            case STUDENT_PAYMENT_HISTORY -> studentPaymentHistory(criteria);
            case STUDENT_CERTIFICATES -> studentCertificates(criteria);
            case STUDENT_LIVE_CLASS_ATTENDANCE -> studentLiveClassAttendance(criteria);
            case INSTRUCTOR_COURSE_ENROLLMENT -> instructorCourseEnrollment(criteria);
            case INSTRUCTOR_STUDENT_PROGRESS -> instructorStudentProgress(criteria);
            case INSTRUCTOR_LIVE_CLASSES -> instructorLiveClasses(criteria);
            case INSTRUCTOR_CERTIFICATES -> instructorCertificates(criteria);
            case ADMIN_USERS -> adminUsers(criteria);
            case ADMIN_COURSES -> adminCourses(criteria);
            case ADMIN_CATEGORIES -> adminCategories(criteria);
            case ADMIN_SUBSCRIPTIONS -> adminSubscriptions(criteria);
            case ADMIN_PAYMENTS -> adminPayments(criteria);
            case ADMIN_LIVE_CLASSES -> adminLiveClasses(criteria);
            case ADMIN_CERTIFICATES -> adminCertificates(criteria);
            case ADMIN_REVENUE -> adminRevenue(criteria);
            case ADMIN_LEARNING_ANALYTICS -> adminLearningAnalytics(criteria);
        };

        // Ref: SRS 15.10 - free-text search. Applied uniformly across every
        // rendered column rather than a per-report column list, so "search"
        // means the same thing (and never silently misses a column) in all
        // seventeen reports.
        if (!criteria.hasSearch()) {
            return rows;
        }
        String needle = criteria.search().toLowerCase();
        return rows.stream().filter(row -> row.values().stream()
                .anyMatch(value -> value != null && value.toString().toLowerCase().contains(needle))).toList();
    }

    // =====================================================================
    // Student reports (SRS 15.4) - scoped to criteria.callerId
    // =====================================================================

    private List<Map<String, Object>> studentMyCourses(ReportCriteria criteria) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Subscription subscription : subscriptionRepository.findByStudentId(criteria.callerId())) {
            if (!matchesCourse(criteria, subscription.getCourseId()) || !inDateRange(criteria, subscription.getStartDate())) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("courseTitle", courseTitle(subscription.getCourseId()));
            row.put("planName", planName(subscription.getSubscriptionPlanId()));
            row.put("status", subscription.getStatus());
            row.put("startDate", subscription.getStartDate());
            row.put("endDate", subscription.getEndDate());
            row.put("completed", courseCompletionRepository
                    .findByStudentIdAndCourseId(criteria.callerId(), subscription.getCourseId()).isPresent());
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, Object>> studentPaymentHistory(ReportCriteria criteria) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Payment payment : paymentRepository.findAll()) {
            if (!payment.getStudentId().equals(criteria.callerId()) || !inDateRange(criteria, payment.getPaymentDate())) {
                continue;
            }
            UUID courseId = orderCourseId(payment.getOrderId());
            if (!matchesCourse(criteria, courseId)) {
                continue;
            }
            rows.add(paymentRow(payment, courseId, null));
        }
        return sortedByDateDesc(rows, "paymentDate");
    }

    private List<Map<String, Object>> studentCertificates(ReportCriteria criteria) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Certificate certificate : certificateRepository.findByStudentId(criteria.callerId())) {
            if (!matchesCourse(criteria, certificate.getCourseId()) || !inDateRange(criteria, certificate.getIssueDate())) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("certificateNumber", certificate.getCertificateNumber());
            row.put("courseTitle", courseTitle(certificate.getCourseId()));
            row.put("completionDate", certificate.getCompletionDate());
            row.put("issueDate", certificate.getIssueDate());
            row.put("verificationId", certificate.getVerificationId());
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, Object>> studentLiveClassAttendance(ReportCriteria criteria) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (LiveClassAttendance attendance : liveClassAttendanceRepository.findByStudentId(criteria.callerId())) {
            LiveClass liveClass = liveClassRepository.findById(attendance.getLiveClassId()).orElse(null);
            if (liveClass == null
                    || !matchesCourse(criteria, liveClass.getCourseId())
                    || !inDateRange(criteria, liveClass.getScheduledDate())) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("liveClassTitle", liveClass.getTitle());
            row.put("courseTitle", courseTitle(liveClass.getCourseId()));
            row.put("scheduledDate", liveClass.getScheduledDate());
            row.put("scheduledTime", liveClass.getScheduledTime());
            row.put("joinedAt", attendance.getJoinedAt());
            rows.add(row);
        }
        return rows;
    }

    // =====================================================================
    // Instructor reports (SRS 15.5) - scoped to the instructor's own courses
    // =====================================================================

    private List<Map<String, Object>> instructorCourseEnrollment(ReportCriteria criteria) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Course course : instructorCourses(criteria)) {
            for (Subscription subscription : subscriptionsForCourse(course.getId())) {
                if (!inDateRange(criteria, subscription.getStartDate())) {
                    continue;
                }
                User student = userRepository.findById(subscription.getStudentId()).orElse(null);
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("courseTitle", course.getTitle());
                row.put("studentName", fullName(student));
                row.put("studentEmail", student == null ? null : student.getEmail());
                row.put("status", subscription.getStatus());
                row.put("startDate", subscription.getStartDate());
                row.put("endDate", subscription.getEndDate());
                rows.add(row);
            }
        }
        return rows;
    }

    private List<Map<String, Object>> instructorStudentProgress(ReportCriteria criteria) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Course course : instructorCourses(criteria)) {
            // Only published lessons count towards progress - a draft lesson is
            // not something a student could have completed (Ref: SRS 11.4).
            List<UUID> lessonIds = lessonRepository.findByCourseIdAndStatus(course.getId(), ContentStatus.PUBLISHED)
                    .stream().map(Lesson::getId).toList();
            int totalLessons = lessonIds.size();

            for (Subscription subscription : subscriptionsForCourse(course.getId())) {
                if (!inDateRange(criteria, subscription.getStartDate())) {
                    continue;
                }
                UUID studentId = subscription.getStudentId();
                long completed = lessonIds.isEmpty() ? 0 : lessonProgressRepository
                        .countByStudentIdAndStatusAndLessonIdIn(studentId, ProgressStatus.COMPLETED, lessonIds);

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("courseTitle", course.getTitle());
                row.put("studentName", fullName(userRepository.findById(studentId).orElse(null)));
                row.put("lessonsCompleted", completed);
                row.put("totalLessons", totalLessons);
                row.put("progressPercent", percent(completed, totalLessons));
                row.put("courseCompleted", courseCompletionRepository
                        .findByStudentIdAndCourseId(studentId, course.getId()).isPresent());
                rows.add(row);
            }
        }
        return rows;
    }

    private List<Map<String, Object>> instructorLiveClasses(ReportCriteria criteria) {
        return liveClassRows(instructorCourses(criteria), criteria);
    }

    private List<Map<String, Object>> instructorCertificates(ReportCriteria criteria) {
        List<UUID> courseIds = instructorCourses(criteria).stream().map(Course::getId).toList();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Certificate certificate : certificateRepository.findAll()) {
            if (!courseIds.contains(certificate.getCourseId()) || !inDateRange(criteria, certificate.getIssueDate())) {
                continue;
            }
            rows.add(certificateRow(certificate));
        }
        return rows;
    }

    // =====================================================================
    // Administrator reports (SRS 15.6) - system-wide
    // =====================================================================

    private List<Map<String, Object>> adminUsers(ReportCriteria criteria) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            if (!inDateRange(criteria, user.getCreatedAt())) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", fullName(user));
            row.put("email", user.getEmail());
            row.put("mobileNumber", user.getMobileNumber());
            row.put("role", user.getRole());
            row.put("status", user.getStatus());
            row.put("registeredAt", user.getCreatedAt());
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, Object>> adminCourses(ReportCriteria criteria) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Course course : courseRepository.findAll()) {
            if (!matchesCourse(criteria, course.getId()) || !inDateRange(criteria, course.getCreatedAt())) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("courseTitle", course.getTitle());
            row.put("status", course.getStatus());
            row.put("difficultyLevel", course.getDifficultyLevel());
            row.put("language", course.getLanguage());
            row.put("instructors", course.getInstructors().stream().map(this::fullName).sorted().toList());
            row.put("subscriberCount", subscriptionRepository.countByCourseId(course.getId()));
            row.put("createdAt", course.getCreatedAt());
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, Object>> adminCategories(ReportCriteria criteria) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Category category : categoryRepository.findAll()) {
            if (!inDateRange(criteria, category.getCreatedAt())) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", category.getName());
            row.put("status", category.getStatus());
            row.put("displayOrder", category.getDisplayOrder());
            row.put("courseCount", courseRepository.countByCategories_Id(category.getId()));
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, Object>> adminSubscriptions(ReportCriteria criteria) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Subscription subscription : subscriptionRepository.findAll()) {
            if (!matchesCourse(criteria, subscription.getCourseId()) || !inDateRange(criteria, subscription.getStartDate())) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("studentName", fullName(userRepository.findById(subscription.getStudentId()).orElse(null)));
            row.put("courseTitle", courseTitle(subscription.getCourseId()));
            row.put("planName", planName(subscription.getSubscriptionPlanId()));
            row.put("status", subscription.getStatus());
            row.put("startDate", subscription.getStartDate());
            row.put("endDate", subscription.getEndDate());
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, Object>> adminPayments(ReportCriteria criteria) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Payment payment : paymentRepository.findAll()) {
            if (!inDateRange(criteria, payment.getPaymentDate())) {
                continue;
            }
            UUID courseId = orderCourseId(payment.getOrderId());
            if (!matchesCourse(criteria, courseId)) {
                continue;
            }
            rows.add(paymentRow(payment, courseId, userRepository.findById(payment.getStudentId()).orElse(null)));
        }
        return sortedByDateDesc(rows, "paymentDate");
    }

    private List<Map<String, Object>> adminLiveClasses(ReportCriteria criteria) {
        return liveClassRows(courseRepository.findAll(), criteria);
    }

    private List<Map<String, Object>> adminCertificates(ReportCriteria criteria) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Certificate certificate : certificateRepository.findAll()) {
            if (!matchesCourse(criteria, certificate.getCourseId()) || !inDateRange(criteria, certificate.getIssueDate())) {
                continue;
            }
            rows.add(certificateRow(certificate));
        }
        return rows;
    }

    /** Ref: SRS 15.6 - revenue, aggregated by calendar month over successful payments only. */
    private List<Map<String, Object>> adminRevenue(ReportCriteria criteria) {
        Map<YearMonth, BigDecimal> totals = new TreeMap<>();
        Map<YearMonth, Long> counts = new HashMap<>();
        Map<YearMonth, String> currencies = new HashMap<>();

        for (Payment payment : paymentRepository.findAll()) {
            if (payment.getStatus() != PaymentStatus.SUCCESS || payment.getPaymentDate() == null) {
                continue;
            }
            if (!inDateRange(criteria, payment.getPaymentDate())) {
                continue;
            }
            if (!matchesCourse(criteria, orderCourseId(payment.getOrderId()))) {
                continue;
            }
            YearMonth month = YearMonth.from(payment.getPaymentDate());
            totals.merge(month, payment.getAmount(), BigDecimal::add);
            counts.merge(month, 1L, Long::sum);
            currencies.putIfAbsent(month, payment.getCurrency());
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        totals.forEach((month, total) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("period", month.toString());
            row.put("successfulPayments", counts.get(month));
            row.put("totalAmount", total);
            row.put("currency", currencies.get(month));
            rows.add(row);
        });
        // Most recent month first - the figure an administrator opens this for.
        rows.sort(Comparator.comparing(row -> String.valueOf(row.get("period")), Comparator.reverseOrder()));
        return rows;
    }

    /** Ref: SRS 15.6 - per-course learning outcomes. */
    private List<Map<String, Object>> adminLearningAnalytics(ReportCriteria criteria) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Course course : courseRepository.findAll()) {
            if (!matchesCourse(criteria, course.getId())) {
                continue;
            }
            long enrolled = subscriptionRepository.countByCourseId(course.getId());
            long completions = courseCompletionRepository.countByCourseIdIn(List.of(course.getId()));
            long certificates = certificateRepository.countByCourseIdIn(List.of(course.getId()));

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("courseTitle", course.getTitle());
            row.put("enrolledStudents", enrolled);
            row.put("courseCompletions", completions);
            row.put("completionRate", percent(completions, enrolled));
            row.put("certificatesIssued", certificates);
            rows.add(row);
        }
        return rows;
    }

    // =====================================================================
    // Shared row builders / helpers
    // =====================================================================

    private List<Map<String, Object>> liveClassRows(List<Course> courses, ReportCriteria criteria) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Course course : courses) {
            if (!matchesCourse(criteria, course.getId())) {
                continue;
            }
            liveClassRepository.findByCourseId(course.getId(), org.springframework.data.domain.Pageable.unpaged())
                    .forEach(liveClass -> {
                        if (!inDateRange(criteria, liveClass.getScheduledDate())) {
                            return;
                        }
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("liveClassTitle", liveClass.getTitle());
                        row.put("courseTitle", course.getTitle());
                        row.put("scheduledDate", liveClass.getScheduledDate());
                        row.put("scheduledTime", liveClass.getScheduledTime());
                        row.put("status", liveClass.getStatus());
                        row.put("attendeeCount", liveClassAttendanceRepository.countByLiveClassId(liveClass.getId()));
                        rows.add(row);
                    });
        }
        return rows;
    }

    private Map<String, Object> paymentRow(Payment payment, UUID courseId, User student) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("paymentDate", payment.getPaymentDate());
        if (student != null) {
            row.put("studentName", fullName(student));
        }
        row.put("courseTitle", courseId == null ? null : courseTitle(courseId));
        row.put("amount", payment.getAmount());
        row.put("currency", payment.getCurrency());
        row.put("paymentMethod", payment.getPaymentMethod());
        row.put("status", payment.getStatus());
        row.put("transactionReference", payment.getTransactionReference());
        return row;
    }

    private Map<String, Object> certificateRow(Certificate certificate) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("certificateNumber", certificate.getCertificateNumber());
        row.put("studentName", fullName(userRepository.findById(certificate.getStudentId()).orElse(null)));
        row.put("courseTitle", courseTitle(certificate.getCourseId()));
        row.put("completionDate", certificate.getCompletionDate());
        row.put("issueDate", certificate.getIssueDate());
        return row;
    }

    private List<Course> instructorCourses(ReportCriteria criteria) {
        return courseRepository.findByInstructors_Id(criteria.callerId()).stream()
                .filter(course -> matchesCourse(criteria, course.getId()))
                .toList();
    }

    private List<Subscription> subscriptionsForCourse(UUID courseId) {
        return subscriptionRepository.findByCourseId(courseId, org.springframework.data.domain.Pageable.unpaged()).getContent();
    }

    private UUID orderCourseId(UUID orderId) {
        return orderRepository.findById(orderId).map(Order::getCourseId).orElse(null);
    }

    private String courseTitle(UUID courseId) {
        return courseRepository.findById(courseId).map(Course::getTitle).orElse(null);
    }

    private String planName(UUID planId) {
        return planId == null ? null : subscriptionPlanRepository.findById(planId).map(SubscriptionPlan::getPlanName).orElse(null);
    }

    private String fullName(User user) {
        return user == null ? null : user.getFirstName() + " " + user.getLastName();
    }

    /** Ref: SRS 15.7 - optional courseId filter; a row with no course never matches a course filter. */
    private boolean matchesCourse(ReportCriteria criteria, UUID courseId) {
        return criteria.courseId() == null || Objects.equals(criteria.courseId(), courseId);
    }

    private boolean inDateRange(ReportCriteria criteria, LocalDate date) {
        if (criteria.dateFrom() == null && criteria.dateTo() == null) {
            return true;
        }
        if (date == null) {
            return false; // an undated row can't fall inside an explicit range
        }
        return (criteria.dateFrom() == null || !date.isBefore(criteria.dateFrom()))
                && (criteria.dateTo() == null || !date.isAfter(criteria.dateTo()));
    }

    private boolean inDateRange(ReportCriteria criteria, OffsetDateTime timestamp) {
        return inDateRange(criteria, timestamp == null ? null : timestamp.toLocalDate());
    }

    private BigDecimal percent(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 1, RoundingMode.HALF_UP);
    }

    private List<Map<String, Object>> sortedByDateDesc(List<Map<String, Object>> rows, String key) {
        return rows.stream()
                .sorted(Comparator.comparing(
                        (Map<String, Object> row) -> (OffsetDateTime) row.get(key),
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }
}
