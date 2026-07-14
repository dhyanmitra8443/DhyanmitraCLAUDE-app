package com.lms.payment.repository;

import com.lms.payment.entity.Payment;
import com.lms.payment.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {

    Page<Payment> findByStudentId(UUID studentId, Pageable pageable);

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    long countByStatus(PaymentStatus status);

    long countByStatusAndPaymentDateBetween(PaymentStatus status, OffsetDateTime from, OffsetDateTime to);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = :status and p.paymentDate between :from and :to")
    BigDecimal sumAmountByStatusAndPaymentDateBetween(
            @Param("status") PaymentStatus status, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
}
