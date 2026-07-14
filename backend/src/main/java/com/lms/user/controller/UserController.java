package com.lms.user.controller;

import com.lms.auth.dto.UserSummary;
import com.lms.config.security.UserPrincipal;
import com.lms.shared.response.ApiResponse;
import com.lms.shared.response.PageResponse;
import com.lms.user.dto.UpdateOwnProfileRequest;
import com.lms.user.dto.UpdateUserStatusRequest;
import com.lms.user.dto.UserFullProfileResponse;
import com.lms.user.dto.UserProfileResponse;
import com.lms.user.entity.UserRole;
import com.lms.user.entity.UserStatus;
import com.lms.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/** Ref: SRS Chapter 4 - User Management. Matches openapi.yaml's Users tag. */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getOwnProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.of("Current user's profile.", userService.getOwnProfile(principal.getUserId())));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateOwnProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateOwnProfileRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of("Profile updated.", userService.updateOwnProfile(principal.getUserId(), request)));
    }

    @PostMapping("/me/photo")
    public ResponseEntity<ApiResponse<Map<String, String>>> updateOwnPhoto(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("file") MultipartFile file
    ) {
        String url = userService.updateProfilePhoto(principal.getUserId(), file);
        return ResponseEntity.ok(ApiResponse.of("Profile photo updated.", Map.of("profilePhotoUrl", url)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<PageResponse<UserSummary>>> searchUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) LocalDate registeredAfter,
            @RequestParam(required = false) LocalDate registeredBefore,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<UserSummary> results = userService.searchUsers(search, role, status, registeredAfter, registeredBefore, pageable);
        return ResponseEntity.ok(ApiResponse.of("Paginated user list.", results));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<UserFullProfileResponse>> getFullProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.of("Full user profile.", userService.getFullProfile(userId)));
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<UserFullProfileResponse>> adminUpdateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateOwnProfileRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of("User updated.", userService.adminUpdateUser(userId, request)));
    }

    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        userService.updateStatus(userId, request.status());
        return ResponseEntity.ok(ApiResponse.of("Status updated."));
    }
}
