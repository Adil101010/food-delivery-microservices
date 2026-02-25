package com.fooddelivery.orderservice.controller;

import com.fooddelivery.orderservice.dto.ApiResponse;
import com.fooddelivery.orderservice.dto.CreateOrderRequest;
import com.fooddelivery.orderservice.dto.MessageResponse;
import com.fooddelivery.orderservice.dto.OrderResponse;
import com.fooddelivery.orderservice.dto.OrderWithPaymentResponse;
import com.fooddelivery.orderservice.dto.PaymentStatusUpdateRequest;
import com.fooddelivery.orderservice.enums.OrderStatus;
import com.fooddelivery.orderservice.service.OrderService;
import com.fooddelivery.orderservice.service.RazorpayWebhookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final RazorpayWebhookService razorpayWebhookService;

    // ✅ Fix 2 — application.properties se secret read karega
    @Value("${internal.secret}")
    private String internalSecret;

    // ─────────────────────────────────────────
    // HEALTH CHECK
    // ─────────────────────────────────────────
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Order Service is running");
    }

    // ─────────────────────────────────────────
    // CREATE ORDER
    // ─────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ApiResponse<OrderWithPaymentResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        log.info("Create order request for user: {}", request.getUserId());
        OrderWithPaymentResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", response));
    }

    // ─────────────────────────────────────────
    // GET ORDER BY ID
    // ─────────────────────────────────────────
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Long orderId) {

        log.info("Fetching order: {}", orderId);
        OrderResponse response = orderService.getOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", response));
    }

    // ─────────────────────────────────────────
    // GET USER ORDERS
    // ─────────────────────────────────────────
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getUserOrders(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching orders for user: {} | page: {} | size: {}", userId, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderResponse> orders = orderService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("User orders retrieved successfully", orders));
    }

    // ─────────────────────────────────────────
    // GET RESTAURANT ORDERS
    // ─────────────────────────────────────────
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getRestaurantOrders(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching orders for restaurant: {}", restaurantId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderResponse> orders = orderService.getRestaurantOrders(restaurantId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Restaurant orders retrieved", orders));
    }

    // ─────────────────────────────────────────
    // UPDATE ORDER STATUS
    // ─────────────────────────────────────────
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {

        log.info("Updating order {} status to: {}", orderId, status);
        OrderResponse response = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", response));
    }

    // ─────────────────────────────────────────
    // CANCEL ORDER
    // ─────────────────────────────────────────
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<MessageResponse>> cancelOrder(
            @PathVariable Long orderId) {

        log.info("Cancelling order: {}", orderId);
        MessageResponse response = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", response));
    }

    // ─────────────────────────────────────────
    // GET ORDERS BY STATUS
    // ─────────────────────────────────────────
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching orders with status: {}", status);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderResponse> orders = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    // ─────────────────────────────────────────
    // VERIFY PAYMENT — Flutter se call hoga
    // ─────────────────────────────────────────
    @PostMapping("/verify-payment")
    public ResponseEntity<ApiResponse<OrderResponse>> verifyPayment(
            @RequestBody PaymentStatusUpdateRequest request) {

        log.info("Flutter payment verify for order: {}", request.getOrderId());

        boolean isValid = razorpayWebhookService.verifyPaymentSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!isValid) {
            log.error("Invalid payment signature for order: {}", request.getOrderId());
            orderService.updatePaymentStatus(
                    request.getOrderId(),
                    com.fooddelivery.orderservice.enums.PaymentStatus.FAILED,
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Payment verification failed. Order cancelled."));
        }

        OrderResponse response = orderService.updatePaymentStatus(
                request.getOrderId(),
                com.fooddelivery.orderservice.enums.PaymentStatus.PAID,
                request.getRazorpayPaymentId()
        );

        log.info("Payment verified successfully for order: {}", request.getOrderId());
        return ResponseEntity.ok(ApiResponse.success("Payment verified! Order confirmed.", response));
    }

    // ─────────────────────────────────────────
    // RAZORPAY WEBHOOK
    // ─────────────────────────────────────────
    @PostMapping("/webhook/razorpay")
    public ResponseEntity<String> razorpayWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {

        log.info("Razorpay webhook received");

        if (signature == null || !razorpayWebhookService.verifyWebhookSignature(payload, signature)) {
            log.error("Invalid webhook signature!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }

        razorpayWebhookService.processWebhook(payload);
        return ResponseEntity.ok("Webhook processed");
    }

    // ─────────────────────────────────────────
    // INTERNAL PAYMENT STATUS UPDATE
    // ✅ Fix 2 — Sirf payment-service call kar sakti hai
    // ─────────────────────────────────────────
    @PostMapping("/webhook/payment-status")
    public ResponseEntity<ApiResponse<OrderResponse>> updatePaymentStatus(
            @RequestHeader(value = "X-Internal-Secret", required = false) String secret,
            @RequestBody PaymentStatusUpdateRequest request) {

        // ✅ Secret validate karo
        if (secret == null || !secret.equals(internalSecret)) {
            log.error("Unauthorized payment-status update attempt for order: {}",
                    request.getOrderId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized request"));
        }

        log.info("Payment status update for order: {} - Status: {}",
                request.getOrderId(), request.getPaymentStatus());

        OrderResponse response = orderService.updatePaymentStatus(
                request.getOrderId(),
                request.getPaymentStatus(),
                request.getRazorpayPaymentId()
        );

        return ResponseEntity.ok(ApiResponse.success("Payment status updated", response));
    }
}
