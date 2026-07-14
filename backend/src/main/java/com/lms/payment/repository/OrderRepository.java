package com.lms.payment.repository;

import com.lms.payment.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    Page<Order> findByStudentId(UUID studentId, Pageable pageable);

    long countByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to);
}
