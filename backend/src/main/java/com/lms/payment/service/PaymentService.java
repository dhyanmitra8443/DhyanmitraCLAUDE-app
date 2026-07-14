package com.lms.payment.service;

import com.lms.config.security.UserPrincipal;
import com.lms.payment.dto.PaymentSummaryResponse;
import com.lms.payment.entity.Payment;
import com.lms.payment.entity.PaymentStatus;
import com.lms.payment.repository.PaymentRepository;
import com.lms.payment.repository.PaymentSpecifications;
import com.lms.shared.exception.ForbiddenException;
import com.lms.shared.exception.ResourceNotFoundException;
import com.lms.shared.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Ref: SRS Chapter 10 - Payment Management (payments). */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentSummaryResponse> getOwnPayments(UUID studentId, Pageable pageable) {
        return PageResponse.from(paymentRepository.findByStudentId(studentId, pageable), this::toSummary);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentSummaryResponse> searchPayments(PaymentStatus status, String transactionReference, Pageable pageable) {
        Specification<Payment> spec = Specification.where(PaymentSpecifications.hasStatus(status))
                .and(PaymentSpecifications.transactionReferenceContains(transactionReference));
        return PageResponse.from(paymentRepository.findAll(spec, pageable), this::toSummary);
    }

    @Transactional(readOnly = true)
    public PaymentSummaryResponse getPaymentDetail(UUID paymentId, UserPrincipal principal) {
        Payment payment = findPaymentOrThrow(paymentId);
        boolean isAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATOR"));
        if (!isAdmin && !payment.getStudentId().equals(principal.getUserId())) {
            throw new ForbiddenException("You do not have access to this payment.");
        }
        return toSummary(payment);
    }

    private PaymentSummaryResponse toSummary(Payment payment) {
        return new PaymentSummaryResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getStudentId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null,
                payment.getTransactionReference(),
                payment.getStatus().name(),
                payment.getPaymentDate()
        );
    }

    private Payment findPaymentOrThrow(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found."));
    }
}
