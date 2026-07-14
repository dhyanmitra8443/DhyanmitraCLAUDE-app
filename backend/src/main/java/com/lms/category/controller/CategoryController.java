package com.lms.category.controller;

import com.lms.category.dto.CategorySummaryResponse;
import com.lms.category.dto.CreateCategoryRequest;
import com.lms.category.dto.UpdateCategoryStatusRequest;
import com.lms.category.entity.CategoryStatus;
import com.lms.category.service.CategoryService;
import com.lms.config.security.UserPrincipal;
import com.lms.shared.response.ApiResponse;
import com.lms.shared.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Ref: SRS Chapter 6 - Category Management. Matches openapi.yaml's Categories tag. */
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CategorySummaryResponse>>> listCategories(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CategoryStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<CategorySummaryResponse> results = categoryService.listCategories(search, status, isAdmin(principal), page, size);
        return ResponseEntity.ok(ApiResponse.of("Category list, sorted by displayOrder then name.", results));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<CategorySummaryResponse>> createCategory(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        CategorySummaryResponse created = categoryService.createCategory(request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("Category created.", created));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategorySummaryResponse>> getCategoryDetail(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(ApiResponse.of("Category details.", categoryService.getCategoryDetail(categoryId)));
    }

    @PatchMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<CategorySummaryResponse>> updateCategory(
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        CategorySummaryResponse updated = categoryService.updateCategory(categoryId, request, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.of("Category updated.", updated));
    }

    @PatchMapping("/{categoryId}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateCategoryStatusRequest request
    ) {
        categoryService.updateStatus(categoryId, request.status(), principal.getUserId());
        return ResponseEntity.ok(ApiResponse.of("Status updated."));
    }

    private boolean isAdmin(UserPrincipal principal) {
        return principal != null && principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATOR"));
    }
}
