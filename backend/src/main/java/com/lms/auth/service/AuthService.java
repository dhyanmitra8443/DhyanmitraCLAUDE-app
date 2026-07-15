package com.lms.auth.service;

import com.lms.auth.dto.AcceptInvitationRequest;
import com.lms.auth.dto.AuthTokenResponse;
import com.lms.auth.dto.ChangePasswordRequest;
import com.lms.auth.dto.ForgotPasswordRequest;
import com.lms.auth.dto.InvitationPreviewResponse;
import com.lms.auth.dto.InviteInstructorRequest;
import com.lms.auth.dto.LoginRequest;
import com.lms.auth.dto.RegisterStudentRequest;
import com.lms.auth.dto.ResetPasswordRequest;
import com.lms.auth.dto.UserSummary;
import com.lms.auth.entity.AuthToken;
import com.lms.auth.entity.AuthTokenType;
import com.lms.auth.entity.InstructorInvitation;
import com.lms.auth.entity.InvitationStatus;
import com.lms.auth.entity.SessionStatus;
import com.lms.auth.entity.UserSession;
import com.lms.auth.repository.AuthTokenRepository;
import com.lms.auth.repository.InstructorInvitationRepository;
import com.lms.auth.repository.UserSessionRepository;
import com.lms.config.security.JwtProperties;
import com.lms.config.security.JwtService;
import com.lms.notification.EmailService;
import com.lms.shared.exception.BadRequestException;
import com.lms.shared.exception.ConflictException;
import com.lms.shared.exception.ResourceNotFoundException;
import com.lms.shared.util.SecureTokenGenerator;
import com.lms.user.entity.User;
import com.lms.user.entity.UserRole;
import com.lms.user.entity.UserStatus;
import com.lms.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Ref: SRS Chapter 3 - Authentication & Authorization. Business logic for
 * every /auth/** endpoint; AuthController is a thin adapter over this.
 */
@Service
public class AuthService {

    private static final Duration PASSWORD_RESET_TTL = Duration.ofHours(1);
    private static final Duration INVITATION_TTL = Duration.ofDays(7);
    private static final String GENERIC_LOGIN_FAILURE = "Invalid email or password, or account is inactive.";

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final AuthTokenRepository authTokenRepository;
    private final InstructorInvitationRepository invitationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final SecureTokenGenerator tokenGenerator;
    private final EmailService emailService;
    private final String frontendUrl;

    public AuthService(
            UserRepository userRepository,
            UserSessionRepository userSessionRepository,
            AuthTokenRepository authTokenRepository,
            InstructorInvitationRepository invitationRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            JwtProperties jwtProperties,
            SecureTokenGenerator tokenGenerator,
            EmailService emailService,
            @Value("${app.frontend.url}") String frontendUrl
    ) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.authTokenRepository = authTokenRepository;
        this.invitationRepository = invitationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.tokenGenerator = tokenGenerator;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
    }

    @Transactional
    public UserSummary register(RegisterStudentRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email is already registered.");
        }
        if (userRepository.existsByMobileNumber(request.mobileNumber())) {
            throw new ConflictException("Mobile number is already registered.");
        }
        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setMobileNumber(request.mobileNumber());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.STUDENT);
        user.setStatus(UserStatus.ACTIVE);
        user = userRepository.save(user);

        emailService.send(user.getEmail(), "Welcome to Dhyan Mitra",
                "Hi " + user.getFirstName() + ", your student account has been created.");

        return UserSummary.from(user);
    }

    @Transactional
    public AuthTokenResponse login(LoginRequest request, String deviceInfo, String ipAddress) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException(GENERIC_LOGIN_FAILURE));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException(GENERIC_LOGIN_FAILURE);
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadCredentialsException(GENERIC_LOGIN_FAILURE);
        }

        // Ref: SRS 3.7 - single active session per user: deactivate any
        // session left over from a previous login before creating a new one.
        deactivateActiveSession(user.getId());

        UserSession session = new UserSession();
        session.setUserId(user.getId());
        session.setDeviceInfo(deviceInfo);
        session.setIpAddress(ipAddress);
        session.setStatus(SessionStatus.ACTIVE);
        session = userSessionRepository.save(session);

        return issueTokenPair(user, session.getId());
    }

    @Transactional
    public AuthTokenResponse refreshToken(String rawRefreshToken) {
        Claims claims = jwtService.parseAndValidate(rawRefreshToken)
                .filter(jwtService::isRefreshToken)
                .orElseThrow(() -> new BadCredentialsException("Refresh token invalid, expired, or revoked."));

        UUID sessionId = jwtService.extractSessionId(claims);
        if (!userSessionRepository.existsByIdAndStatus(sessionId, SessionStatus.ACTIVE)) {
            throw new BadCredentialsException("Refresh token invalid, expired, or revoked.");
        }

        AuthToken storedToken = authTokenRepository
                .findByTokenHashAndTokenType(tokenGenerator.hash(rawRefreshToken), AuthTokenType.REFRESH)
                .filter(t -> t.getUsedAt() == null && t.getRevokedAt() == null)
                .filter(t -> t.getExpiresAt().isAfter(OffsetDateTime.now()))
                .orElseThrow(() -> new BadCredentialsException("Refresh token invalid, expired, or revoked."));

        // Ref: SRS 3.12 - refresh tokens are single-use; rotate on every exchange.
        storedToken.setUsedAt(OffsetDateTime.now());
        authTokenRepository.save(storedToken);

        User user = userRepository.findById(jwtService.extractUserId(claims))
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        return issueTokenPair(user, sessionId);
    }

    @Transactional
    public void logout(UUID sessionId) {
        userSessionRepository.findById(sessionId).ifPresent(session -> {
            if (session.getStatus() == SessionStatus.ACTIVE) {
                session.setStatus(SessionStatus.INACTIVE);
                session.setLogoutAt(OffsetDateTime.now());
                userSessionRepository.save(session);
            }
        });
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Always behaves identically regardless of whether the email exists,
        // to avoid leaking account existence (Ref: SRS 3.10 intent + openapi.yaml).
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            String rawToken = tokenGenerator.generate();
            AuthToken resetToken = new AuthToken();
            resetToken.setUserId(user.getId());
            resetToken.setTokenType(AuthTokenType.PASSWORD_RESET);
            resetToken.setTokenHash(tokenGenerator.hash(rawToken));
            resetToken.setExpiresAt(OffsetDateTime.now().plus(PASSWORD_RESET_TTL));
            authTokenRepository.save(resetToken);

            String resetUrl = frontendUrl + "/reset-password?token=" + rawToken;
            emailService.send(user.getEmail(), "Reset your Dhyan Mitra password",
                    "Reset your password: " + resetUrl
                            + "\n\nOr use this token directly: " + rawToken);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        AuthToken resetToken = authTokenRepository
                .findByTokenHashAndTokenType(tokenGenerator.hash(request.token()), AuthTokenType.PASSWORD_RESET)
                .filter(t -> t.getUsedAt() == null && t.getRevokedAt() == null)
                .filter(t -> t.getExpiresAt().isAfter(OffsetDateTime.now()))
                .orElseThrow(() -> new BadRequestException("Token invalid, expired, or already used."));

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetToken.setUsedAt(OffsetDateTime.now());
        authTokenRepository.save(resetToken);

        // Ref: SRS 3.10 - "Invalidates all existing sessions on success."
        deactivateActiveSession(user.getId());
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Ref: SRS 3.11 - "Invalidates existing sessions on success; user must log in again."
        deactivateActiveSession(user.getId());
    }

    @Transactional
    public void inviteInstructor(InviteInstructorRequest request, UUID invitedByAdminId) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("A user with this email already exists.");
        }
        String rawToken = tokenGenerator.generate();
        InstructorInvitation invitation = new InstructorInvitation();
        invitation.setEmail(request.email());
        invitation.setFirstName(request.firstName());
        invitation.setLastName(request.lastName());
        invitation.setTokenHash(tokenGenerator.hash(rawToken));
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setInvitedBy(invitedByAdminId);
        invitation.setExpiresAt(OffsetDateTime.now().plus(INVITATION_TTL));
        invitationRepository.save(invitation);

        String acceptUrl = frontendUrl + "/accept-invitation?token=" + rawToken;
        emailService.send(request.email(), "You're invited to join Dhyan Mitra as an instructor",
                "Accept your invitation: " + acceptUrl
                        + "\n\nOr use this token directly: " + rawToken);
    }

    public InvitationPreviewResponse validateInvitation(String rawToken) {
        InstructorInvitation invitation = findPendingInvitation(rawToken);
        return new InvitationPreviewResponse(invitation.getEmail(), invitation.getFirstName(), invitation.getLastName());
    }

    @Transactional
    public UserSummary acceptInvitation(AcceptInvitationRequest request) {
        InstructorInvitation invitation = findPendingInvitation(request.token());
        if (userRepository.existsByEmail(invitation.getEmail())) {
            throw new ConflictException("A user with this email already exists.");
        }
        if (userRepository.existsByMobileNumber(request.mobileNumber())) {
            throw new ConflictException("Mobile number is already registered.");
        }

        User user = new User();
        user.setFirstName(invitation.getFirstName());
        user.setLastName(invitation.getLastName());
        user.setEmail(invitation.getEmail());
        // Ref: SRS 4.7 - collected here (not at invite time) since the admin
        // invites by email/name only; the instructor supplies their own
        // contact number when activating the account.
        user.setMobileNumber(request.mobileNumber());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.INSTRUCTOR);
        user.setStatus(UserStatus.ACTIVE);
        user = userRepository.save(user);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(OffsetDateTime.now());
        invitationRepository.save(invitation);

        return UserSummary.from(user);
    }

    private InstructorInvitation findPendingInvitation(String rawToken) {
        InstructorInvitation invitation = invitationRepository.findByTokenHash(tokenGenerator.hash(rawToken))
                .orElseThrow(() -> new BadRequestException("Token invalid, expired, or already used."));
        if (invitation.getStatus() != InvitationStatus.PENDING || invitation.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new BadRequestException("Token invalid, expired, or already used.");
        }
        return invitation;
    }

    private void deactivateActiveSession(UUID userId) {
        userSessionRepository.findByUserIdAndStatus(userId, SessionStatus.ACTIVE).ifPresent(session -> {
            session.setStatus(SessionStatus.INACTIVE);
            session.setLogoutAt(OffsetDateTime.now());
            // saveAndFlush, not save: Hibernate's flush order runs all pending
            // INSERTs before UPDATEs regardless of call order, so a plain
            // save() here would let the new session's INSERT hit the
            // partial-unique-active-session index before this UPDATE lands.
            userSessionRepository.saveAndFlush(session);
        });
    }

    private AuthTokenResponse issueTokenPair(User user, UUID sessionId) {
        String role = user.getRole().name();
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), role, sessionId);
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail(), role, sessionId);

        AuthToken refreshTokenRecord = new AuthToken();
        refreshTokenRecord.setUserId(user.getId());
        refreshTokenRecord.setSessionId(sessionId);
        refreshTokenRecord.setTokenType(AuthTokenType.REFRESH);
        refreshTokenRecord.setTokenHash(tokenGenerator.hash(refreshToken));
        refreshTokenRecord.setExpiresAt(OffsetDateTime.now().plusSeconds(jwtProperties.refreshTokenTtlSeconds()));
        authTokenRepository.save(refreshTokenRecord);

        return new AuthTokenResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtProperties.accessTokenTtlSeconds(),
                UserSummary.from(user)
        );
    }
}
