package com.lms.notification.controller;

import com.lms.config.security.UserPrincipal;
import com.lms.notification.dto.NotificationSummaryResponse;
import com.lms.notification.entity.NotificationType;
import com.lms.notification.entity.ReadStatus;
import com.lms.notification.service.NotificationService;
import com.lms.shared.response.ApiResponse;
import com.lms.shared.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Ref: SRS Chapter 14 - Notification Management. Matches openapi.yaml's Notifications tag. */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PageResponse<NotificationSummaryResponse>>> getOwnNotifications(
            @RequestParam(required = false) ReadStatus readStatus,
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<NotificationSummaryResponse> results = notificationService.getOwnNotifications(principal.getUserId(), readStatus, pageable);
        return ResponseEntity.ok(ApiResponse.of("Paginated notification list.", results));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable UUID notificationId, @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markRead(notificationId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.of("Notification marked as read."));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(@AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllRead(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.of("All notifications marked as read."));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<PageResponse<NotificationSummaryResponse>>> searchNotifications(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String notificationType,
            @RequestParam(required = false) NotificationType deliveryChannel,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<NotificationSummaryResponse> results = notificationService.searchNotifications(userId, notificationType, deliveryChannel, pageable);
        return ResponseEntity.ok(ApiResponse.of("Paginated system-wide notification log.", results));
    }
}
