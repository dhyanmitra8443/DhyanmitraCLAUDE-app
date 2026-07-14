package com.lms.payment.repository;

import com.lms.payment.entity.RazorpayWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RazorpayWebhookEventRepository extends JpaRepository<RazorpayWebhookEvent, UUID> {

    boolean existsByRazorpayEventId(String razorpayEventId);

    Optional<RazorpayWebhookEvent> findByRazorpayEventId(String razorpayEventId);
}
