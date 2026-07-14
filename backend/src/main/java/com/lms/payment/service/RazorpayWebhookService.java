package com.lms.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.payment.RazorpayConfigResolver;
import com.lms.payment.entity.Order;
import com.lms.payment.entity.OrderStatus;
import com.lms.payment.entity.Payment;
import com.lms.payment.entity.PaymentMethod;
import com.lms.payment.entity.PaymentStatus;
import com.lms.payment.entity.RazorpayWebhookEvent;
import com.lms.payment.repository.OrderRepository;
import com.lms.payment.repository.PaymentRepository;
import com.lms.payment.repository.RazorpayWebhookEventRepository;
import com.lms.notification.entity.NotificationType;
import com.lms.notification.service.NotificationService;
import com.lms.shared.exception.BadRequestException;
import com.lms.subscription.entity.SubscriptionPlan;
import com.lms.subscription.repository.SubscriptionPlanRepository;
import com.lms.subscription.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Ref: SRS 10.12 - "Webhook as Source of Truth, Signature Verification,
 * Idempotency." Payment status is finalized ONLY here, never through the
 * client-side checkout callback.
 */
@Service
public class RazorpayWebhookService {

    private static final Logger log = LoggerFactory.getLogger(RazorpayWebhookService.class);

    private final RazorpayConfigResolver razorpayConfigResolver;
    private final RazorpayWebhookEventRepository webhookEventRepository;
    private final WebhookEventRecorder webhookEventRecorder;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionService subscriptionService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public RazorpayWebhookService(
            RazorpayConfigResolver razorpayConfigResolver,
            RazorpayWebhookEventRepository webhookEventRepository,
            WebhookEventRecorder webhookEventRecorder,
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            SubscriptionPlanRepository subscriptionPlanRepository,
            SubscriptionService subscriptionService,
            NotificationService notificationService,
            ObjectMapper objectMapper
    ) {
        this.razorpayConfigResolver = razorpayConfigResolver;
        this.webhookEventRepository = webhookEventRepository;
        this.webhookEventRecorder = webhookEventRecorder;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.subscriptionService = subscriptionService;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void processWebhook(String rawBody, String signatureHeader, String eventIdHeader) {
        if (!verifySignature(rawBody, signatureHeader)) {
            throw new BadRequestException("Signature verification failed.");
        }
        if (eventIdHeader == null || eventIdHeader.isBlank()) {
            throw new BadRequestException("Missing X-Razorpay-Event-Id header.");
        }

        JsonNode body = parseBody(rawBody);
        String eventType = body.path("event").asText("");

        // Ref: SRS 10.12 - runs in its own REQUIRES_NEW transaction (see
        // WebhookEventRecorder) so a duplicate delivery's constraint
        // violation can't poison this method's own transaction.
        if (!webhookEventRecorder.tryRecord(eventIdHeader, eventType, rawBody)) {
            log.info("Duplicate Razorpay webhook delivery for event {} - idempotent no-op.", eventIdHeader);
            return;
        }

        JsonNode paymentEntity = body.path("payload").path("payment").path("entity");
        switch (eventType) {
            case "payment.captured" -> handlePaymentCaptured(paymentEntity);
            case "payment.failed" -> handlePaymentFailed(paymentEntity);
            default -> log.info("Received unhandled Razorpay event type '{}' - recorded, no action taken.", eventType);
        }

        RazorpayWebhookEvent event = webhookEventRepository.findByRazorpayEventId(eventIdHeader).orElseThrow();
        event.setProcessedAt(OffsetDateTime.now());
        webhookEventRepository.save(event);
    }

    private void handlePaymentCaptured(JsonNode paymentEntity) {
        String razorpayOrderId = paymentEntity.path("order_id").asText(null);
        Optional<Payment> maybePayment = paymentRepository.findByRazorpayOrderId(razorpayOrderId);
        if (maybePayment.isEmpty()) {
            log.warn("payment.captured for unknown razorpayOrderId {} - no matching Payment attempt found.", razorpayOrderId);
            return;
        }

        Payment payment = maybePayment.get();
        payment.setTransactionReference(paymentEntity.path("id").asText(null));
        payment.setPaymentMethod(mapPaymentMethod(paymentEntity));
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaymentDate(OffsetDateTime.now());
        paymentRepository.save(payment);

        Order order = orderRepository.findById(payment.getOrderId()).orElseThrow();
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        SubscriptionPlan plan = subscriptionPlanRepository.findById(order.getSubscriptionPlanId()).orElseThrow();
        subscriptionService.activateOrRenew(order.getStudentId(), order.getCourseId(), plan.getId(), plan.getDuration(), plan.getDurationUnit());

        notificationService.create(
                order.getStudentId(),
                "Payment Successful",
                "Your payment of " + payment.getAmount() + " " + payment.getCurrency() + " was successful.",
                NotificationType.IN_APP,
                "PAYMENT",
                payment.getId());
    }

    private void handlePaymentFailed(JsonNode paymentEntity) {
        String razorpayOrderId = paymentEntity.path("order_id").asText(null);
        paymentRepository.findByRazorpayOrderId(razorpayOrderId).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            orderRepository.findById(payment.getOrderId()).ifPresent(order -> {
                order.setStatus(OrderStatus.FAILED);
                orderRepository.save(order);
            });

            notificationService.create(
                    payment.getStudentId(),
                    "Payment Failed",
                    "Your payment of " + payment.getAmount() + " " + payment.getCurrency() + " could not be completed.",
                    NotificationType.IN_APP,
                    "PAYMENT",
                    payment.getId());
        });
    }

    private PaymentMethod mapPaymentMethod(JsonNode paymentEntity) {
        String method = paymentEntity.path("method").asText("");
        return switch (method) {
            case "upi" -> PaymentMethod.UPI;
            case "netbanking" -> PaymentMethod.NET_BANKING;
            case "wallet" -> PaymentMethod.WALLET;
            case "card" -> "debit".equalsIgnoreCase(paymentEntity.path("card").path("type").asText(""))
                    ? PaymentMethod.DEBIT_CARD
                    : PaymentMethod.CREDIT_CARD;
            default -> null;
        };
    }

    private boolean verifySignature(String rawBody, String signatureHeader) {
        if (signatureHeader == null || signatureHeader.isBlank()) {
            return false;
        }
        // Ref: SRS 16.9 - the administrator-configured webhook secret wins over
        // the environment default; resolved per delivery so rotating the secret
        // in the admin UI takes effect immediately.
        String webhookSecret = razorpayConfigResolver.resolve().webhookSecret();
        if (webhookSecret == null || webhookSecret.isBlank()) {
            // No secret configured anywhere: fail closed. An unverifiable
            // webhook must never be treated as authentic (SRS 10.12).
            log.error("Razorpay webhook secret is not configured - rejecting delivery.");
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String computed = HexFormat.of().formatHex(hash);
            return computed.equalsIgnoreCase(signatureHeader);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC verification failed", e);
        }
    }

    private JsonNode parseBody(String rawBody) {
        try {
            return objectMapper.readTree(rawBody);
        } catch (Exception e) {
            throw new BadRequestException("Malformed webhook payload.");
        }
    }
}
