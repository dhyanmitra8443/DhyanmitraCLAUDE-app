package com.lms.payment.service;

import com.lms.config.security.UserPrincipal;
import com.lms.course.service.CourseService;
import com.lms.payment.RazorpayProperties;
import com.lms.payment.client.RazorpayClient;
import com.lms.payment.dto.CreateOrderRequest;
import com.lms.payment.dto.OrderSummaryResponse;
import com.lms.payment.dto.RazorpayOrderResponse;
import com.lms.payment.entity.Order;
import com.lms.payment.entity.OrderStatus;
import com.lms.payment.entity.Payment;
import com.lms.payment.entity.PaymentStatus;
import com.lms.payment.repository.OrderRepository;
import com.lms.payment.repository.OrderSpecifications;
import com.lms.payment.repository.PaymentRepository;
import com.lms.shared.exception.BadRequestException;
import com.lms.shared.exception.ForbiddenException;
import com.lms.shared.exception.ResourceNotFoundException;
import com.lms.shared.response.PageResponse;
import com.lms.subscription.entity.PlanStatus;
import com.lms.subscription.entity.SubscriptionPlan;
import com.lms.subscription.repository.SubscriptionPlanRepository;
import com.lms.user.entity.User;
import com.lms.user.repository.UserRepository;
import com.lms.user.repository.UserSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/** Ref: SRS Chapter 10 - Payment Management (orders). */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserRepository userRepository;
    private final CourseService courseService;
    private final RazorpayClient razorpayClient;
    private final RazorpayProperties razorpayProperties;

    public OrderService(
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            SubscriptionPlanRepository subscriptionPlanRepository,
            UserRepository userRepository,
            CourseService courseService,
            RazorpayClient razorpayClient,
            RazorpayProperties razorpayProperties
    ) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.userRepository = userRepository;
        this.courseService = courseService;
        this.razorpayClient = razorpayClient;
        this.razorpayProperties = razorpayProperties;
    }

    @Transactional
    public OrderSummaryResponse createOrder(UUID studentId, CreateOrderRequest request) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.subscriptionPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found."));
        if (!plan.getCourseId().equals(request.courseId())) {
            throw new BadRequestException("subscriptionPlanId does not belong to courseId.");
        }
        if (plan.getStatus() != PlanStatus.ACTIVE) {
            throw new BadRequestException("This subscription plan is not currently available for purchase.");
        }

        Order order = new Order();
        order.setStudentId(studentId);
        order.setCourseId(request.courseId());
        order.setSubscriptionPlanId(plan.getId());
        order.setAmount(plan.getPrice());
        order.setCurrency(plan.getCurrency());
        order.setStatus(OrderStatus.PENDING);
        // saveAndFlush, not save: @Transactional only flushes at commit
        // (i.e. after this whole method returns), so a plain save() here
        // would leave createdAt/updatedAt null in toSummary() below -
        // @Generated on BaseEntity only populates them once the INSERT
        // actually reaches the DB, which a bare save() does not guarantee
        // happens before the response is built.
        return toSummary(orderRepository.saveAndFlush(order));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> searchOrders(String studentName, OrderStatus status, Pageable pageable) {
        Specification<Order> spec = Specification.where(OrderSpecifications.hasStatus(status));
        if (studentName != null && !studentName.isBlank()) {
            List<UUID> matchingStudentIds = userRepository.findAll(UserSpecifications.search(studentName)).stream()
                    .map(User::getId).toList();
            spec = spec.and(OrderSpecifications.studentIdIn(matchingStudentIds));
        }
        return PageResponse.from(orderRepository.findAll(spec, pageable), this::toSummary);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getOwnOrders(UUID studentId, Pageable pageable) {
        return PageResponse.from(orderRepository.findByStudentId(studentId, pageable), this::toSummary);
    }

    @Transactional(readOnly = true)
    public OrderSummaryResponse getOrderDetail(UUID orderId, UserPrincipal principal) {
        Order order = findOrderOrThrow(orderId);
        assertOwnerOrAdmin(order, principal);
        return toSummary(order);
    }

    @Transactional
    public RazorpayOrderResponse createRazorpayOrder(UUID orderId, UserPrincipal principal) {
        Order order = findOrderOrThrow(orderId);
        if (!order.getStudentId().equals(principal.getUserId())) {
            throw new ForbiddenException("This order does not belong to you.");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Only a PENDING order can start checkout.");
        }

        long amountInPaise = order.getAmount().multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact();
        RazorpayClient.RazorpayOrder razorpayOrder = razorpayClient.createOrder(amountInPaise, order.getCurrency(), order.getId().toString());

        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setStudentId(order.getStudentId());
        payment.setAmount(order.getAmount());
        payment.setCurrency(order.getCurrency());
        payment.setRazorpayOrderId(razorpayOrder.id());
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        return new RazorpayOrderResponse(razorpayOrder.id(), razorpayProperties.keyId(), razorpayOrder.amount(), razorpayOrder.currency());
    }

    private void assertOwnerOrAdmin(Order order, UserPrincipal principal) {
        boolean isAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATOR"));
        if (!isAdmin && !order.getStudentId().equals(principal.getUserId())) {
            throw new ForbiddenException("You do not have access to this order.");
        }
    }

    private OrderSummaryResponse toSummary(Order order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getStudentId(),
                order.getCourseId(),
                courseService.getCourseSummary(order.getCourseId()),
                order.getSubscriptionPlanId(),
                order.getAmount(),
                order.getCurrency(),
                order.getStatus().name(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private Order findOrderOrThrow(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found."));
    }
}
