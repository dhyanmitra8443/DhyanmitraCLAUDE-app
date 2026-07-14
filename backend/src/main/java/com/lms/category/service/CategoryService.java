package com.lms.category.service;

import com.lms.category.dto.CategorySummaryResponse;
import com.lms.category.dto.CreateCategoryRequest;
import com.lms.category.entity.Category;
import com.lms.category.entity.CategoryStatus;
import com.lms.category.repository.CategoryRepository;
import com.lms.category.repository.CategorySpecifications;
import com.lms.course.repository.CourseRepository;
import com.lms.shared.exception.BadRequestException;
import com.lms.shared.exception.ConflictException;
import com.lms.shared.exception.ResourceNotFoundException;
import com.lms.shared.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Ref: SRS Chapter 6 - Category Management. */
@Service
public class CategoryService {

    // Ref: SRS 6.10 - fixed server-side sort; the contract exposes no sort
    // param for this endpoint, unlike Courses/Users.
    private static final Sort DEFAULT_SORT = Sort.by("displayOrder").ascending().and(Sort.by("name").ascending());

    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;

    public CategoryService(CategoryRepository categoryRepository, CourseRepository courseRepository) {
        this.categoryRepository = categoryRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CategorySummaryResponse> listCategories(String search, CategoryStatus status, boolean isAdmin, int page, int size) {
        Specification<Category> spec = Specification.where(CategorySpecifications.nameContains(search));

        if (isAdmin) {
            spec = spec.and(CategorySpecifications.hasStatus(status));
        } else {
            // Ref: SRS 6.9, 6.11 - public callers only ever see ACTIVE
            // categories that have at least one PUBLISHED course.
            spec = spec.and(CategorySpecifications.hasStatus(CategoryStatus.ACTIVE))
                    .and(CategorySpecifications.hasPublishedCourse());
        }

        Page<Category> result = categoryRepository.findAll(spec, PageRequest.of(page, size, DEFAULT_SORT));
        return PageResponse.from(result, this::toSummary);
    }

    @Transactional(readOnly = true)
    public CategorySummaryResponse getCategoryDetail(UUID categoryId) {
        return toSummary(findCategoryOrThrow(categoryId));
    }

    @Transactional
    public CategorySummaryResponse createCategory(CreateCategoryRequest request, UUID adminUserId) {
        if (categoryRepository.existsByName(request.name())) {
            throw new ConflictException("A category with this name already exists.");
        }
        Category category = new Category();
        applyRequest(category, request);
        category.setCreatedBy(adminUserId);
        category.setUpdatedBy(adminUserId);
        return toSummary(categoryRepository.save(category));
    }

    @Transactional
    public CategorySummaryResponse updateCategory(UUID categoryId, CreateCategoryRequest request, UUID adminUserId) {
        Category category = findCategoryOrThrow(categoryId);
        if (categoryRepository.existsByNameAndIdNot(request.name(), categoryId)) {
            throw new ConflictException("A category with this name already exists.");
        }
        applyRequest(category, request);
        category.setUpdatedBy(adminUserId);
        return toSummary(categoryRepository.save(category));
    }

    @Transactional
    public void updateStatus(UUID categoryId, String rawStatus, UUID adminUserId) {
        Category category = findCategoryOrThrow(categoryId);
        category.setStatus(parseStatus(rawStatus));
        category.setUpdatedBy(adminUserId);
        categoryRepository.save(category);
    }

    private CategoryStatus parseStatus(String rawStatus) {
        try {
            return CategoryStatus.valueOf(rawStatus);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("status must be one of: " + List.of(CategoryStatus.values()));
        }
    }

    private void applyRequest(Category category, CreateCategoryRequest request) {
        category.setName(request.name());
        category.setDescription(request.description());
        category.setIconUrl(request.iconUrl());
        category.setDisplayOrder(request.displayOrder());
    }

    private CategorySummaryResponse toSummary(Category category) {
        return CategorySummaryResponse.from(category, courseRepository.countByCategories_Id(category.getId()));
    }

    private Category findCategoryOrThrow(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found."));
    }
}
