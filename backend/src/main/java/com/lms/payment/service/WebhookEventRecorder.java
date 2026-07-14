package com.lms.payment.service;

import com.lms.payment.entity.RazorpayWebhookEvent;
import com.lms.payment.repository.RazorpayWebhookEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ref: SRS 10.12 - isolated in its own REQUIRES_NEW transaction and its
 * own bean (self-invocation from RazorpayWebhookService would bypass
 * Spring's transactional proxy entirely).
 *
 * Check-then-insert, not insert-then-catch: once a flush fails inside a
 * transaction, Spring/Hibernate mark that transaction rollback-only at the
 * infrastructure level - catching the resulting DataIntegrityViolationException
 * in Java code does NOT undo that, so even THIS method's own REQUIRES_NEW
 * transaction would still fail with UnexpectedRollbackException on commit
 * despite "handling" the exception. Checking existence first avoids ever
 * triggering the violation in the common (already-seen event) case; the
 * narrow remaining race (two truly simultaneous first deliveries of the
 * same event) surfaces as an uncaught 409 for the loser, which Razorpay's
 * standard webhook retry then resolves as a clean idempotent no-op.
 */
@Service
public class WebhookEventRecorder {

    private static final Logger log = LoggerFactory.getLogger(WebhookEventRecorder.class);

    private final RazorpayWebhookEventRepository webhookEventRepository;

    public WebhookEventRecorder(RazorpayWebhookEventRepository webhookEventRepository) {
        this.webhookEventRepository = webhookEventRepository;
    }

    /** Returns true if this event id was newly recorded (proceed with processing), false if it's a duplicate (no-op). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryRecord(String eventId, String eventType, String rawBody) {
        if (webhookEventRepository.existsByRazorpayEventId(eventId)) {
            log.info("Duplicate Razorpay webhook delivery for event {} - idempotent no-op.", eventId);
            return false;
        }
        RazorpayWebhookEvent event = new RazorpayWebhookEvent();
        event.setRazorpayEventId(eventId);
        event.setEventType(eventType);
        event.setPayload(rawBody);
        webhookEventRepository.save(event);
        return true;
    }
}
