package com.lms.settings.controller;

import com.lms.config.security.UserPrincipal;
import com.lms.settings.dto.AuthenticationSettingsRequest;
import com.lms.settings.dto.BackupSettingsRequest;
import com.lms.settings.dto.CertificateSettingsRequest;
import com.lms.settings.dto.EmailSettingsRequest;
import com.lms.settings.dto.FileUploadSettingsRequest;
import com.lms.settings.dto.GeneralSettingsRequest;
import com.lms.settings.dto.LiveClassSettingsRequest;
import com.lms.settings.dto.MaintenanceModeRequest;
import com.lms.settings.dto.NotificationSettingsRequest;
import com.lms.settings.dto.PaymentGatewaySettingsRequest;
import com.lms.settings.dto.SystemSettingsResponse;
import com.lms.settings.service.SystemSettingsService;
import com.lms.shared.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ref: SRS Chapter 16 - System Settings. Matches openapi.yaml's Settings tag.
 *
 * Administrator-only throughout (x-roles: [ADMINISTRATOR]); the class-level
 * @PreAuthorize applies to every endpoint here, so no individual method can
 * accidentally ship without a role check.
 */
@RestController
@RequestMapping("/api/v1/settings")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class SettingsController {

    private final SystemSettingsService settingsService;

    public SettingsController(SystemSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<SystemSettingsResponse>> getSettings() {
        return ResponseEntity.ok(ApiResponse.of("System settings.", settingsService.getSettings()));
    }

    @PatchMapping("/general")
    public ResponseEntity<ApiResponse<Void>> updateGeneral(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody GeneralSettingsRequest request
    ) {
        settingsService.updateGeneral(request, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.of("General settings updated."));
    }

    @PatchMapping("/authentication")
    public ResponseEntity<ApiResponse<Void>> updateAuthentication(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AuthenticationSettingsRequest request
    ) {
        settingsService.updateAuthentication(request, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.of("Authentication settings updated."));
    }

    @PatchMapping("/file-upload")
    public ResponseEntity<ApiResponse<Void>> updateFileUpload(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody FileUploadSettingsRequest request
    ) {
        settingsService.updateFileUpload(request, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.of("File upload settings updated."));
    }

    @PatchMapping("/email")
    public ResponseEntity<ApiResponse<Void>> updateEmail(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody EmailSettingsRequest request
    ) {
        settingsService.updateEmail(request, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.of("Email settings updated."));
    }

    @PatchMapping("/notifications")
    public ResponseEntity<ApiResponse<Void>> updateNotifications(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody NotificationSettingsRequest request
    ) {
        settingsService.updateNotifications(request, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.of("Notification settings updated."));
    }

    @PatchMapping("/payment-gateway")
    public ResponseEntity<ApiResponse<Void>> updatePaymentGateway(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PaymentGatewaySettingsRequest request
    ) {
        settingsService.updatePaymentGateway(request, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.of("Payment gateway settings updated."));
    }

    @PatchMapping("/live-class")
    public ResponseEntity<ApiResponse<Void>> updateLiveClass(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody LiveClassSettingsRequest request
    ) {
        settingsService.updateLiveClass(request, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.of("Live class defaults updated."));
    }

    @PatchMapping("/certificate")
    public ResponseEntity<ApiResponse<Void>> updateCertificate(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CertificateSettingsRequest request
    ) {
        settingsService.updateCertificate(request, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.of("Certificate settings updated."));
    }

    @PatchMapping("/maintenance-mode")
    public ResponseEntity<ApiResponse<Void>> updateMaintenanceMode(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody MaintenanceModeRequest request
    ) {
        settingsService.updateMaintenanceMode(request.enabled(), principal.getUserId());
        return ResponseEntity.ok(ApiResponse.of(
                request.enabled() ? "Maintenance mode enabled." : "Maintenance mode disabled."));
    }

    @GetMapping("/backup-configuration")
    public ResponseEntity<ApiResponse<SystemSettingsResponse.Backup>> getBackupConfiguration() {
        return ResponseEntity.ok(ApiResponse.of("Backup configuration.", settingsService.getBackupSettings()));
    }

    @PatchMapping("/backup-configuration")
    public ResponseEntity<ApiResponse<Void>> updateBackupConfiguration(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BackupSettingsRequest request
    ) {
        settingsService.updateBackup(request, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.of("Backup configuration updated."));
    }
}
