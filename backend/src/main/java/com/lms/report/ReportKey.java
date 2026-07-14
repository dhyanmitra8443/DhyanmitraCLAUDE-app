package com.lms.report;

import com.lms.user.entity.UserRole;

import java.util.Arrays;
import java.util.List;

/**
 * Ref: SRS 15.4 (student), 15.5 (instructor), 15.6 (administrator) - the
 * complete catalogue of reports, matching openapi.yaml's ReportKey enum.
 *
 * Each report belongs to exactly one role (SRS 15.3: "Reports shall be
 * role-specific"), and that ownership is the authorization rule: a caller
 * may only run reports whose role matches their own. Administrators are
 * deliberately *not* granted the student/instructor keys - their own nine
 * reports already span the whole system, and letting an admin run
 * STUDENT_MY_COURSES would be meaningless anyway since those reports are
 * scoped to the caller's own data (SRS 15.13).
 *
 * The column list is part of the definition rather than being inferred from
 * the returned rows, so an export still has correct headers when the result
 * set is empty.
 */
public enum ReportKey {

    // --- Student (SRS 15.4) - always scoped to the calling student --------
    STUDENT_MY_COURSES("My Courses", UserRole.STUDENT, List.of(
            column("courseTitle", "Course"),
            column("planName", "Plan"),
            column("status", "Status"),
            column("startDate", "Start Date"),
            column("endDate", "End Date"),
            column("completed", "Completed"))),

    STUDENT_PAYMENT_HISTORY("Payment History", UserRole.STUDENT, List.of(
            column("paymentDate", "Date"),
            column("courseTitle", "Course"),
            column("amount", "Amount"),
            column("currency", "Currency"),
            column("paymentMethod", "Method"),
            column("status", "Status"),
            column("transactionReference", "Transaction Reference"))),

    STUDENT_CERTIFICATES("My Certificates", UserRole.STUDENT, List.of(
            column("certificateNumber", "Certificate No."),
            column("courseTitle", "Course"),
            column("completionDate", "Completion Date"),
            column("issueDate", "Issue Date"),
            column("verificationId", "Verification ID"))),

    STUDENT_LIVE_CLASS_ATTENDANCE("Live Class Attendance", UserRole.STUDENT, List.of(
            column("liveClassTitle", "Live Class"),
            column("courseTitle", "Course"),
            column("scheduledDate", "Scheduled Date"),
            column("scheduledTime", "Scheduled Time"),
            column("joinedAt", "Joined At"))),

    // --- Instructor (SRS 15.5) - scoped to the instructor's own courses ---
    INSTRUCTOR_COURSE_ENROLLMENT("Course Enrollment", UserRole.INSTRUCTOR, List.of(
            column("courseTitle", "Course"),
            column("studentName", "Student"),
            column("studentEmail", "Email"),
            column("status", "Subscription Status"),
            column("startDate", "Start Date"),
            column("endDate", "End Date"))),

    INSTRUCTOR_STUDENT_PROGRESS("Student Progress", UserRole.INSTRUCTOR, List.of(
            column("courseTitle", "Course"),
            column("studentName", "Student"),
            column("lessonsCompleted", "Lessons Completed"),
            column("totalLessons", "Total Lessons"),
            column("progressPercent", "Progress %"),
            column("courseCompleted", "Course Completed"))),

    INSTRUCTOR_LIVE_CLASSES("My Live Classes", UserRole.INSTRUCTOR, List.of(
            column("liveClassTitle", "Live Class"),
            column("courseTitle", "Course"),
            column("scheduledDate", "Scheduled Date"),
            column("scheduledTime", "Scheduled Time"),
            column("status", "Status"),
            column("attendeeCount", "Attendees"))),

    INSTRUCTOR_CERTIFICATES("Certificates Issued", UserRole.INSTRUCTOR, List.of(
            column("certificateNumber", "Certificate No."),
            column("studentName", "Student"),
            column("courseTitle", "Course"),
            column("completionDate", "Completion Date"),
            column("issueDate", "Issue Date"))),

    // --- Administrator (SRS 15.6) - system-wide ---------------------------
    ADMIN_USERS("Users", UserRole.ADMINISTRATOR, List.of(
            column("name", "Name"),
            column("email", "Email"),
            column("mobileNumber", "Mobile"),
            column("role", "Role"),
            column("status", "Status"),
            column("registeredAt", "Registered"))),

    ADMIN_COURSES("Courses", UserRole.ADMINISTRATOR, List.of(
            column("courseTitle", "Course"),
            column("status", "Status"),
            column("difficultyLevel", "Level"),
            column("language", "Language"),
            column("instructors", "Instructors"),
            column("subscriberCount", "Subscribers"),
            column("createdAt", "Created"))),

    ADMIN_CATEGORIES("Categories", UserRole.ADMINISTRATOR, List.of(
            column("name", "Category"),
            column("status", "Status"),
            column("displayOrder", "Display Order"),
            column("courseCount", "Courses"))),

    ADMIN_SUBSCRIPTIONS("Subscriptions", UserRole.ADMINISTRATOR, List.of(
            column("studentName", "Student"),
            column("courseTitle", "Course"),
            column("planName", "Plan"),
            column("status", "Status"),
            column("startDate", "Start Date"),
            column("endDate", "End Date"))),

    ADMIN_PAYMENTS("Payments", UserRole.ADMINISTRATOR, List.of(
            column("paymentDate", "Date"),
            column("studentName", "Student"),
            column("courseTitle", "Course"),
            column("amount", "Amount"),
            column("currency", "Currency"),
            column("paymentMethod", "Method"),
            column("status", "Status"),
            column("transactionReference", "Transaction Reference"))),

    ADMIN_LIVE_CLASSES("Live Classes", UserRole.ADMINISTRATOR, List.of(
            column("liveClassTitle", "Live Class"),
            column("courseTitle", "Course"),
            column("scheduledDate", "Scheduled Date"),
            column("scheduledTime", "Scheduled Time"),
            column("status", "Status"),
            column("attendeeCount", "Attendees"))),

    ADMIN_CERTIFICATES("Certificates", UserRole.ADMINISTRATOR, List.of(
            column("certificateNumber", "Certificate No."),
            column("studentName", "Student"),
            column("courseTitle", "Course"),
            column("completionDate", "Completion Date"),
            column("issueDate", "Issue Date"))),

    ADMIN_REVENUE("Revenue", UserRole.ADMINISTRATOR, List.of(
            column("period", "Month"),
            column("successfulPayments", "Successful Payments"),
            column("totalAmount", "Revenue"),
            column("currency", "Currency"))),

    ADMIN_LEARNING_ANALYTICS("Learning Analytics", UserRole.ADMINISTRATOR, List.of(
            column("courseTitle", "Course"),
            column("enrolledStudents", "Enrolled"),
            column("courseCompletions", "Course Completions"),
            column("completionRate", "Completion Rate %"),
            column("certificatesIssued", "Certificates Issued")));

    private final String title;
    private final UserRole role;
    private final List<Column> columns;

    ReportKey(String title, UserRole role, List<Column> columns) {
        this.title = title;
        this.role = role;
        this.columns = columns;
    }

    public String title() {
        return title;
    }

    public UserRole role() {
        return role;
    }

    public List<Column> columns() {
        return columns;
    }

    /** Ref: SRS 15.3 - the reports this role is permitted to run. */
    public static List<ReportKey> availableTo(UserRole role) {
        return Arrays.stream(values()).filter(key -> key.role == role).toList();
    }

    public boolean isAvailableTo(UserRole role) {
        return this.role == role;
    }

    /** A single column: the key used in the row map, and the header shown in exports. */
    public record Column(String key, String label) {
    }

    private static Column column(String key, String label) {
        return new Column(key, label);
    }
}
