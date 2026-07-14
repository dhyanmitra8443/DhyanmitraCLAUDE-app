package com.lms.user.service;

import com.lms.auth.dto.UserSummary;
import com.lms.auth.entity.SessionStatus;
import com.lms.auth.repository.UserSessionRepository;
import com.lms.shared.exception.BadRequestException;
import com.lms.shared.exception.ResourceNotFoundException;
import com.lms.shared.response.PageResponse;
import com.lms.shared.storage.FileStorageService;
import com.lms.user.dto.UpdateOwnProfileRequest;
import com.lms.user.dto.UserFullProfileResponse;
import com.lms.user.dto.UserProfileResponse;
import com.lms.user.entity.User;
import com.lms.user.entity.UserRole;
import com.lms.user.entity.UserStatus;
import com.lms.user.repository.UserRepository;
import com.lms.user.repository.UserSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Ref: SRS Chapter 4 - User Management. */
@Service
public class UserService {

    private static final Set<String> ALLOWED_PHOTO_TYPES = Set.of("image/jpeg", "image/png");
    private static final long MAX_PHOTO_BYTES = 5L * 1024 * 1024;

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final FileStorageService fileStorageService;

    public UserService(UserRepository userRepository, UserSessionRepository userSessionRepository, FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.fileStorageService = fileStorageService;
    }

    public UserProfileResponse getOwnProfile(UUID userId) {
        return UserProfileResponse.from(findUserOrThrow(userId));
    }

    @Transactional
    public UserProfileResponse updateOwnProfile(UUID userId, UpdateOwnProfileRequest request) {
        User user = findUserOrThrow(userId);
        applyProfileUpdate(user, request);
        return UserProfileResponse.from(userRepository.save(user));
    }

    @Transactional
    public String updateProfilePhoto(UUID userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("A file is required.");
        }
        if (!ALLOWED_PHOTO_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Only JPG and PNG images are allowed.");
        }
        if (file.getSize() > MAX_PHOTO_BYTES) {
            throw new BadRequestException("File exceeds the maximum allowed size of 5 MB.");
        }

        User user = findUserOrThrow(userId);
        String url = fileStorageService.store(file, "profile-photos");
        user.setProfilePhotoUrl(url);
        userRepository.save(user);
        return url;
    }

    public PageResponse<UserSummary> searchUsers(
            String search, UserRole role, UserStatus status, LocalDate registeredAfter, LocalDate registeredBefore, Pageable pageable
    ) {
        Specification<User> spec = Specification.where(UserSpecifications.search(search))
                .and(UserSpecifications.hasRole(role))
                .and(UserSpecifications.hasStatus(status))
                .and(UserSpecifications.registeredAfter(registeredAfter))
                .and(UserSpecifications.registeredBefore(registeredBefore));

        Page<User> page = userRepository.findAll(spec, pageable);
        return PageResponse.from(page, UserSummary::from);
    }

    public UserFullProfileResponse getFullProfile(UUID userId) {
        return UserFullProfileResponse.from(findUserOrThrow(userId));
    }

    @Transactional
    public UserFullProfileResponse adminUpdateUser(UUID userId, UpdateOwnProfileRequest request) {
        User user = findUserOrThrow(userId);
        applyProfileUpdate(user, request);
        return UserFullProfileResponse.from(userRepository.save(user));
    }

    @Transactional
    public void updateStatus(UUID userId, String rawStatus) {
        UserStatus newStatus = parseStatus(rawStatus);
        User user = findUserOrThrow(userId);
        user.setStatus(newStatus);
        userRepository.save(user);

        // Ref: SRS 4.4/4.5 intent - a blocked/deactivated account should not
        // remain usable via an already-issued token.
        if (newStatus != UserStatus.ACTIVE) {
            userSessionRepository.findByUserIdAndStatus(userId, SessionStatus.ACTIVE).ifPresent(session -> {
                session.setStatus(SessionStatus.INACTIVE);
                session.setLogoutAt(OffsetDateTime.now());
                userSessionRepository.save(session);
            });
        }
    }

    private UserStatus parseStatus(String rawStatus) {
        try {
            return UserStatus.valueOf(rawStatus);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("status must be one of: " + List.of(UserStatus.values()));
        }
    }

    private void applyProfileUpdate(User user, UpdateOwnProfileRequest request) {
        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.mobileNumber() != null) user.setMobileNumber(request.mobileNumber());
        if (request.dateOfBirth() != null) user.setDateOfBirth(request.dateOfBirth());
        if (request.gender() != null) user.setGender(request.gender());

        // Ref: SRS 4.7 - instructor-only fields; the server ignores them for
        // any other role rather than rejecting the request outright.
        if (user.getRole() == UserRole.INSTRUCTOR) {
            if (request.professionalBio() != null) user.setProfessionalBio(request.professionalBio());
            if (request.yearsOfExperience() != null) user.setYearsOfExperience(request.yearsOfExperience());
            if (request.specializations() != null) user.setSpecializations(request.specializations());
        }
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }
}
