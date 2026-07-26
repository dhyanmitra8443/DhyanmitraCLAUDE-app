package com.lms.referral.service;

import com.lms.notification.EmailService;
import com.lms.referral.dto.AcceptReferralRequest;
import com.lms.referral.dto.AdminReferralResponse;
import com.lms.referral.dto.CreateReferralRequest;
import com.lms.referral.dto.ReferralPreviewResponse;
import com.lms.referral.dto.ReferralSummaryResponse;
import com.lms.referral.entity.Referral;
import com.lms.referral.entity.ReferralStatus;
import com.lms.referral.repository.ReferralRepository;
import com.lms.referral.repository.ReferralSpecifications;
import com.lms.shared.exception.BadRequestException;
import com.lms.shared.exception.ConflictException;
import com.lms.shared.response.PageResponse;
import com.lms.shared.util.SecureTokenGenerator;
import com.lms.user.entity.User;
import com.lms.user.entity.UserRole;
import com.lms.user.entity.UserStatus;
import com.lms.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Business logic for member referrals. The invite/accept mechanics deliberately
 * mirror {@code AuthService}'s instructor-invitation flow (single-use hashed
 * token, 7-day expiry, frontend link) - the only differences are that any
 * logged-in STUDENT/INSTRUCTOR can create one and the accepted account is always
 * a STUDENT.
 */
@Service
public class ReferralService {

    private static final Duration REFERRAL_TTL = Duration.ofDays(7);
    private static final Sort NEWEST_FIRST = Sort.by("createdAt").descending();

    private final ReferralRepository referralRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureTokenGenerator tokenGenerator;
    private final EmailService emailService;
    private final String frontendUrl;

    public ReferralService(
            ReferralRepository referralRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            SecureTokenGenerator tokenGenerator,
            EmailService emailService,
            @Value("${app.frontend.url}") String frontendUrl
    ) {
        this.referralRepository = referralRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenGenerator = tokenGenerator;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
    }

    @Transactional
    public void createReferral(CreateReferralRequest request, UUID referrerId) {
        User referrer = userRepository.findById(referrerId)
                .orElseThrow(() -> new BadRequestException("Referrer account not found."));

        if (referrer.getEmail().equalsIgnoreCase(request.email())) {
            throw new BadRequestException("You cannot refer your own account.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("A user with this email already exists.");
        }
        if (referralRepository.existsByRefereeEmailAndReferrerIdAndStatus(request.email(), referrerId, ReferralStatus.PENDING)) {
            throw new ConflictException("You already have a pending referral for this email.");
        }

        String rawToken = tokenGenerator.generate();
        Referral referral = new Referral();
        referral.setReferrerId(referrerId);
        referral.setRefereeFirstName(request.firstName());
        referral.setRefereeLastName(request.lastName());
        referral.setRefereeEmail(request.email());
        referral.setTokenHash(tokenGenerator.hash(rawToken));
        referral.setStatus(ReferralStatus.PENDING);
        referral.setExpiresAt(OffsetDateTime.now().plus(REFERRAL_TTL));
        referralRepository.save(referral);

        String acceptUrl = frontendUrl + "/accept-referral?token=" + rawToken;
        String referrerName = fullName(referrer);
        emailService.send(request.email(), referrerName + " invited you to join Dhyan Mitra",
                "Hi " + request.firstName() + ",\n\n" + referrerName
                        + " has invited you to join Dhyan Mitra. Accept your invitation and create your account: "
                        + acceptUrl + "\n\nOr use this token directly: " + rawToken);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReferralSummaryResponse> listMyReferrals(UUID referrerId, int page, int size) {
        Page<Referral> result = referralRepository.findByReferrerId(referrerId, PageRequest.of(page, size, NEWEST_FIRST));
        Map<UUID, User> referredUsers = resolveUsers(result.getContent().stream().map(Referral::getReferredUserId));
        return PageResponse.from(result, referral -> toSummary(referral, referredUsers));
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminReferralResponse> listAllReferrals(String search, ReferralStatus status, int page, int size) {
        Specification<Referral> spec = Specification.where(ReferralSpecifications.refereeMatches(search))
                .and(ReferralSpecifications.hasStatus(status));
        Page<Referral> result = referralRepository.findAll(spec, PageRequest.of(page, size, NEWEST_FIRST));

        Map<UUID, User> users = resolveUsers(result.getContent().stream()
                .flatMap(r -> java.util.stream.Stream.of(r.getReferrerId(), r.getReferredUserId())));
        return PageResponse.from(result, referral -> toAdminResponse(referral, users));
    }

    public ReferralPreviewResponse validateReferral(String rawToken) {
        Referral referral = findPendingReferral(rawToken);
        String referrerName = userRepository.findById(referral.getReferrerId())
                .map(ReferralService::fullName)
                .orElse("A Dhyan Mitra member");
        return new ReferralPreviewResponse(
                referral.getRefereeFirstName(),
                referral.getRefereeLastName(),
                referral.getRefereeEmail(),
                referrerName);
    }

    @Transactional
    public void acceptReferral(AcceptReferralRequest request) {
        Referral referral = findPendingReferral(request.token());
        if (userRepository.existsByEmail(referral.getRefereeEmail())) {
            throw new ConflictException("A user with this email already exists.");
        }
        if (userRepository.existsByMobileNumber(request.mobileNumber())) {
            throw new ConflictException("Mobile number is already registered.");
        }

        User user = new User();
        user.setFirstName(referral.getRefereeFirstName());
        user.setLastName(referral.getRefereeLastName());
        user.setEmail(referral.getRefereeEmail());
        user.setMobileNumber(request.mobileNumber());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.STUDENT);
        user.setStatus(UserStatus.ACTIVE);
        user = userRepository.save(user);

        referral.setStatus(ReferralStatus.ACCEPTED);
        referral.setReferredUserId(user.getId());
        referral.setAcceptedAt(OffsetDateTime.now());
        referralRepository.save(referral);

        emailService.send(user.getEmail(), "Welcome to Dhyan Mitra",
                "Hi " + user.getFirstName() + ", your student account has been created.");
    }

    private Referral findPendingReferral(String rawToken) {
        Referral referral = referralRepository.findByTokenHash(tokenGenerator.hash(rawToken))
                .orElseThrow(() -> new BadRequestException("Token invalid, expired, or already used."));
        if (referral.getStatus() != ReferralStatus.PENDING || referral.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new BadRequestException("Token invalid, expired, or already used.");
        }
        return referral;
    }

    /** Batch-loads the referenced users (skipping nulls) into an id -> User map, avoiding N+1 lookups per row. */
    private Map<UUID, User> resolveUsers(java.util.stream.Stream<UUID> ids) {
        Set<UUID> distinct = ids.filter(java.util.Objects::nonNull).collect(Collectors.toCollection(HashSet::new));
        if (distinct.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(distinct).stream().collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private ReferralSummaryResponse toSummary(Referral referral, Map<UUID, User> referredUsers) {
        User referred = referral.getReferredUserId() == null ? null : referredUsers.get(referral.getReferredUserId());
        return new ReferralSummaryResponse(
                referral.getId(),
                referral.getRefereeFirstName(),
                referral.getRefereeLastName(),
                referral.getRefereeEmail(),
                referral.getStatus().name(),
                referred == null ? null : referred.getStatus().name(),
                referral.getCreatedAt(),
                referral.getAcceptedAt());
    }

    private AdminReferralResponse toAdminResponse(Referral referral, Map<UUID, User> users) {
        User referrer = users.get(referral.getReferrerId());
        User referred = referral.getReferredUserId() == null ? null : users.get(referral.getReferredUserId());
        return new AdminReferralResponse(
                referral.getId(),
                referral.getReferrerId(),
                referrer == null ? null : fullName(referrer),
                referrer == null ? null : referrer.getEmail(),
                referral.getRefereeFirstName(),
                referral.getRefereeLastName(),
                referral.getRefereeEmail(),
                referral.getStatus().name(),
                referral.getReferredUserId(),
                referred == null ? null : referred.getStatus().name(),
                referral.getCreatedAt(),
                referral.getAcceptedAt());
    }

    private static String fullName(User user) {
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }
}
