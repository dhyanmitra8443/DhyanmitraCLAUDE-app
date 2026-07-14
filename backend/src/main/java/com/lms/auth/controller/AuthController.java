package com.lms.auth.controller;

import com.lms.auth.dto.AcceptInvitationRequest;
import com.lms.auth.dto.AuthTokenResponse;
import com.lms.auth.dto.ChangePasswordRequest;
import com.lms.auth.dto.ForgotPasswordRequest;
import com.lms.auth.dto.InvitationPreviewResponse;
import com.lms.auth.dto.InviteInstructorRequest;
import com.lms.auth.dto.LoginRequest;
import com.lms.auth.dto.RefreshTokenRequest;
import com.lms.auth.dto.RegisterStudentRequest;
import com.lms.auth.dto.ResetPasswordRequest;
import com.lms.auth.dto.UserSummary;
import com.lms.auth.service.AuthService;
import com.lms.config.security.UserPrincipal;
import com.lms.shared.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Ref: SRS Chapter 3 - Authentication & Authorization. Matches openapi.yaml's Auth tag. */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserSummary>> register(@Valid @RequestBody RegisterStudentRequest request) {
        UserSummary summary = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("Student registered successfully.", summary));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthTokenResponse tokens = authService.login(request, httpRequest.getHeader("User-Agent"), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.of("Login successful.", tokens));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserPrincipal principal) {
        authService.logout(principal.getSessionId());
        return ResponseEntity.ok(ApiResponse.of("Logged out."));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthTokenResponse tokens = authService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.of("New access token issued.", tokens));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.of("If the email exists, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.of("Password reset successfully."));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.of("Password changed."));
    }

    @PostMapping("/instructors/invitations")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> inviteInstructor(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody InviteInstructorRequest request
    ) {
        authService.inviteInstructor(request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("Invitation created and email queued."));
    }

    @GetMapping("/instructors/invitations/{token}")
    public ResponseEntity<ApiResponse<InvitationPreviewResponse>> validateInvitation(@PathVariable String token) {
        InvitationPreviewResponse preview = authService.validateInvitation(token);
        return ResponseEntity.ok(ApiResponse.of("Token is valid.", preview));
    }

    @PostMapping("/instructors/accept-invitation")
    public ResponseEntity<ApiResponse<UserSummary>> acceptInvitation(@Valid @RequestBody AcceptInvitationRequest request) {
        UserSummary summary = authService.acceptInvitation(request);
        return ResponseEntity.ok(ApiResponse.of("Instructor account activated.", summary));
    }
}
