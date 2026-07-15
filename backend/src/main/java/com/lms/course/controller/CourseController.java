package com.lms.course.controller;

import com.lms.config.security.UserPrincipal;
import com.lms.course.dto.AssignCategoriesRequest;
import com.lms.course.dto.AssignInstructorRequest;
import com.lms.course.dto.CourseDetailResponse;
import com.lms.course.dto.CourseSummaryResponse;
import com.lms.course.dto.CreateCourseRequest;
import com.lms.course.entity.CourseStatus;
import com.lms.course.entity.DifficultyLevel;
import com.lms.course.service.CourseService;
import com.lms.shared.response.ApiResponse;
import com.lms.shared.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Ref: SRS Chapter 5 - Course Management. Matches openapi.yaml's Courses tag. */
@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CourseSummaryResponse>>> listCourses(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<UUID> categoryId,
            @RequestParam(required = false) UUID instructorId,
            @RequestParam(required = false) DifficultyLevel difficultyLevel,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) CourseStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<CourseSummaryResponse> results = courseService.listCourses(
                search, categoryId, instructorId, difficultyLevel, language, status, isAdmin(principal), pageable);
        return ResponseEntity.ok(ApiResponse.of("Paginated course list.", results));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<CourseDetailResponse>> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("Course created as DRAFT.", courseService.createCourse(request)));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseDetailResponse>> getCourseDetail(@PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.of("Course details.", courseService.getCourseDetail(courseId)));
    }

    @PatchMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<CourseDetailResponse>> updateCourse(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateCourseRequest request
    ) {
        courseService.updateCourse(courseId, request, principal);
        return ResponseEntity.ok(ApiResponse.of("Course updated.", courseService.getCourseDetail(courseId)));
    }

    @PostMapping("/{courseId}/publish")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> publishCourse(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        courseService.publishCourse(courseId, principal);
        return ResponseEntity.ok(ApiResponse.of("Course published."));
    }

    @PostMapping("/{courseId}/archive")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> archiveCourse(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        courseService.archiveCourse(courseId, principal);
        return ResponseEntity.ok(ApiResponse.of("Course archived."));
    }

    @PostMapping("/{courseId}/instructors")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> assignInstructor(
            @PathVariable UUID courseId,
            @Valid @RequestBody AssignInstructorRequest request
    ) {
        courseService.assignInstructor(courseId, request.instructorId());
        return ResponseEntity.ok(ApiResponse.of("Instructor assigned."));
    }

    @DeleteMapping("/{courseId}/instructors/{instructorId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> removeInstructor(
            @PathVariable UUID courseId,
            @PathVariable UUID instructorId
    ) {
        courseService.removeInstructor(courseId, instructorId);
        return ResponseEntity.ok(ApiResponse.of("Instructor removed from course."));
    }

    @PostMapping("/{courseId}/categories")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> assignCategories(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AssignCategoriesRequest request
    ) {
        courseService.assignCategories(courseId, request.categoryIds(), principal);
        return ResponseEntity.ok(ApiResponse.of("Categories assigned."));
    }

    @DeleteMapping("/{courseId}/categories/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> removeCategory(
            @PathVariable UUID courseId,
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        courseService.removeCategory(courseId, categoryId, principal);
        return ResponseEntity.ok(ApiResponse.of("Category removed from course."));
    }

    private boolean isAdmin(UserPrincipal principal) {
        return principal != null && principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATOR"));
    }
}
