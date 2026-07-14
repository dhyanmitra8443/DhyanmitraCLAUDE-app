package com.lms.certificate.service;

import com.lms.certificate.CertificatePdfGenerator;
import com.lms.certificate.CertificateProperties;
import com.lms.certificate.dto.CertificateDownloadUrlResponse;
import com.lms.certificate.dto.CertificateSummaryResponse;
import com.lms.certificate.dto.CertificateVerifyResponse;
import com.lms.certificate.entity.Certificate;
import com.lms.certificate.repository.CertificateRepository;
import com.lms.certificate.repository.CertificateSpecifications;
import com.lms.config.security.UserPrincipal;
import com.lms.course.dto.CourseSummaryResponse;
import com.lms.course.service.CourseService;
import com.lms.notification.entity.NotificationType;
import com.lms.notification.service.NotificationService;
import com.lms.settings.CertificateBranding;
import com.lms.settings.service.SystemSettingsService;
import com.lms.shared.exception.ForbiddenException;
import com.lms.shared.exception.ResourceNotFoundException;
import com.lms.shared.response.PageResponse;
import com.lms.shared.storage.FileStorageService;
import com.lms.shared.storage.SignedUrlService;
import com.lms.user.entity.User;
import com.lms.user.repository.UserRepository;
import com.lms.user.repository.UserSpecifications;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Ref: SRS Chapter 12 - Certificate Management. */
@Service
public class CertificateService {

    private static final Duration DOWNLOAD_URL_TTL = Duration.ofMinutes(5);

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final CourseService courseService;
    private final CertificatePdfGenerator pdfGenerator;
    private final CertificateProperties certificateProperties;
    private final FileStorageService fileStorageService;
    private final SignedUrlService signedUrlService;
    private final NotificationService notificationService;
    private final SystemSettingsService settingsService;

    public CertificateService(
            CertificateRepository certificateRepository,
            UserRepository userRepository,
            CourseService courseService,
            CertificatePdfGenerator pdfGenerator,
            CertificateProperties certificateProperties,
            FileStorageService fileStorageService,
            SignedUrlService signedUrlService,
            NotificationService notificationService,
            SystemSettingsService settingsService
    ) {
        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
        this.courseService = courseService;
        this.pdfGenerator = pdfGenerator;
        this.certificateProperties = certificateProperties;
        this.fileStorageService = fileStorageService;
        this.signedUrlService = signedUrlService;
        this.notificationService = notificationService;
        this.settingsService = settingsService;
    }

    /** Ref: SRS 12.9, 12.10 - called by ProgressService the moment a course is first completed. */
    @Transactional
    public void issueIfEligible(UUID studentId, UUID courseId, OffsetDateTime completedAt) {
        if (certificateRepository.findByStudentIdAndCourseId(studentId, courseId).isPresent()) {
            return;
        }

        User student = userRepository.findById(studentId).orElseThrow();
        CourseSummaryResponse course = courseService.getCourseSummary(courseId);
        List<String> instructorNames = course.instructors().stream()
                .map(i -> i.firstName() + " " + i.lastName()).toList();

        // Ref: SRS 16.11 - branding is read at issuance and snapshotted onto the
        // certificate, so a later settings change never alters an already-issued
        // one. The administrator-configured organization name wins; the
        // app.certificate.* environment value is the fallback for a system where
        // nobody has visited the settings screen yet.
        //
        // Only the organization name is applied today: the certificate template
        // (certificates/template.pdf) is fixed artwork with hardcoded field
        // coordinates, so the logo, signature and footer stored by
        // PATCH /settings/certificate have nowhere to render until the generator
        // becomes template-driven. Those three settings persist but are inert.
        CertificateBranding branding = settingsService.certificateBranding();
        String organizationName = branding.organizationName() != null && !branding.organizationName().isBlank()
                ? branding.organizationName()
                : certificateProperties.organizationName();

        Certificate certificate = new Certificate();
        certificate.setStudentId(studentId);
        certificate.setCourseId(courseId);
        certificate.setCompletionDate(completedAt.toLocalDate());
        certificate.setIssueDate(LocalDate.now());
        certificate.setOrganizationNameSnapshot(organizationName);
        certificate.setInstructorNamesSnapshot(instructorNames.toArray(new String[0]));
        certificate.setCertificateNumber(generateCertificateNumber());
        certificate.setVerificationId(UUID.randomUUID());

        byte[] pdfBytes = pdfGenerator.generate(new CertificatePdfGenerator.CertificateFields(
                student.getFirstName() + " " + student.getLastName(),
                course.title(),
                certificate.getCompletionDate(),
                certificate.getCertificateNumber(),
                certificate.getVerificationId().toString(),
                instructorNames.isEmpty() ? "" : String.join(", ", instructorNames),
                certificateProperties.founderName()
        ));

        String reference = "certificates/" + UUID.randomUUID() + ".pdf";
        fileStorageService.writePrivate(pdfBytes, reference);
        certificate.setFileReference(reference);

        certificateRepository.save(certificate);

        notificationService.create(
                studentId,
                "Certificate Issued",
                "Your certificate for \"" + course.title() + "\" is ready to download.",
                NotificationType.IN_APP,
                "CERTIFICATE",
                certificate.getId());
    }

    @Transactional(readOnly = true)
    public List<CertificateSummaryResponse> getOwnCertificates(UUID studentId) {
        return certificateRepository.findByStudentId(studentId).stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<CertificateSummaryResponse> searchCertificates(String studentName, UUID courseId, String certificateNumber, Pageable pageable) {
        Specification<Certificate> spec = Specification.where(CertificateSpecifications.hasCourse(courseId))
                .and(CertificateSpecifications.certificateNumberContains(certificateNumber));
        if (studentName != null && !studentName.isBlank()) {
            List<UUID> matchingStudentIds = userRepository.findAll(UserSpecifications.search(studentName)).stream()
                    .map(User::getId).toList();
            spec = spec.and(CertificateSpecifications.studentIdIn(matchingStudentIds));
        }
        return PageResponse.from(certificateRepository.findAll(spec, pageable), this::toSummary);
    }

    @Transactional(readOnly = true)
    public CertificateSummaryResponse getCertificateDetail(UUID certificateId, UserPrincipal principal) {
        Certificate certificate = findCertificateOrThrow(certificateId);
        assertAccess(certificate, principal);
        return toSummary(certificate);
    }

    @Transactional(readOnly = true)
    public CertificateDownloadUrlResponse issueDownloadUrl(UUID certificateId, UserPrincipal principal) {
        Certificate certificate = findCertificateOrThrow(certificateId);
        assertAccess(certificate, principal);
        String token = signedUrlService.issue(certificateId.toString(), DOWNLOAD_URL_TTL);
        return new CertificateDownloadUrlResponse("/api/v1/certificate-files/download/" + token, DOWNLOAD_URL_TTL.toSeconds());
    }

    @Transactional(readOnly = true)
    public Resource consumeDownload(String token) {
        UUID certificateId = signedUrlService.validate(token)
                .map(UUID::fromString)
                .orElseThrow(() -> new ResourceNotFoundException("Download link is invalid or has expired."));
        Certificate certificate = findCertificateOrThrow(certificateId);
        return fileStorageService.loadPrivate(certificate.getFileReference());
    }

    /** Ref: SRS 12.13, 12.17 - public, unauthenticated; unknown IDs must reveal nothing. */
    @Transactional(readOnly = true)
    public CertificateVerifyResponse verifyPublic(UUID verificationId) {
        Certificate certificate = certificateRepository.findByVerificationId(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found."));
        User student = userRepository.findById(certificate.getStudentId()).orElseThrow();
        CourseSummaryResponse course = courseService.getCourseSummary(certificate.getCourseId());
        return new CertificateVerifyResponse(
                true,
                student.getFirstName() + " " + student.getLastName(),
                course.title(),
                certificate.getCompletionDate(),
                certificate.getIssueDate(),
                certificate.getCertificateNumber()
        );
    }

    private void assertAccess(Certificate certificate, UserPrincipal principal) {
        // Ref: SRS 12.11 - "Administrator, assigned instructor, or the owning student."
        if (certificate.getStudentId().equals(principal.getUserId())) {
            return;
        }
        if (courseService.isAdminOrAssignedInstructor(certificate.getCourseId(), principal)) {
            return;
        }
        throw new ForbiddenException("You do not have access to this certificate.");
    }

    private String generateCertificateNumber() {
        String prefix = "DM-" + LocalDate.now().getYear() + "-";
        long nextSequence = certificateRepository.countByCertificateNumberStartingWith(prefix) + 1;
        return prefix + String.format("%06d", nextSequence);
    }

    private CertificateSummaryResponse toSummary(Certificate certificate) {
        User student = userRepository.findById(certificate.getStudentId()).orElseThrow();
        CourseSummaryResponse course = courseService.getCourseSummary(certificate.getCourseId());
        return new CertificateSummaryResponse(
                certificate.getId(),
                certificate.getCertificateNumber(),
                certificate.getVerificationId(),
                certificate.getStudentId(),
                student.getFirstName() + " " + student.getLastName(),
                certificate.getCourseId(),
                course.title(),
                List.of(certificate.getInstructorNamesSnapshot()),
                certificate.getCompletionDate(),
                certificate.getIssueDate()
        );
    }

    private Certificate findCertificateOrThrow(UUID certificateId) {
        return certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found."));
    }
}
