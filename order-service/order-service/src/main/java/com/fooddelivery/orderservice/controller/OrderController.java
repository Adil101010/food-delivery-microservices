package com.fooddelivery.orderservice.controller;

import com.fooddelivery.orderservice.dto.ApiResponse;
import com.fooddelivery.orderservice.dto.CreateOrderRequest;
import com.fooddelivery.orderservice.dto.MessageResponse;
import com.fooddelivery.orderservice.dto.OrderResponse;
import com.fooddelivery.orderservice.dto.OrderWithPaymentResponse;
import com.fooddelivery.orderservice.dto.PaymentStatusUpdateRequest;
import com.fooddelivery.orderservice.enums.OrderStatus;
import com.fooddelivery.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Order Service is running");
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderWithPaymentResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        log.info("Received create order request for user: {}", request.getUserId());

        OrderWithPaymentResponse response = orderService.createOrder(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", response));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long orderId) {

        log.info("Fetching order with ID: {}", orderId);

        OrderResponse response = orderService.getOrderById(orderId);

        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", response));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getUserOrders(@PathVariable Long userId) {

        log.info("Fetching orders for user: {}", userId);

        List<OrderResponse> orders = orderService.getUserOrders(userId);

        return ResponseEntity.ok(ApiResponse.success("User orders retrieved successfully", orders));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getRestaurantOrders(@PathVariable Long restaurantId) {

        log.info("Fetching orders for restaurant: {}", restaurantId);

        List<OrderResponse> orders = orderService.getRestaurantOrders(restaurantId);

        return ResponseEntity.ok(ApiResponse.success("Restaurant orders retrieved successfully", orders));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {

        log.info("Updating order {} status to: {}", orderId, status);

        OrderResponse response = orderService.updateOrderStatus(orderId, status);

        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", response));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<MessageResponse>> cancelOrder(@PathVariable Long orderId) {

        log.info("Cancelling order: {}", orderId);

        MessageResponse response = orderService.cancelOrder(orderId);

        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", response));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByStatus(@PathVariable OrderStatus status) {

        log.info("Fetching orders with status: {}", status);

        List<OrderResponse> orders = orderService.getOrdersByStatus(status);

        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    @PostMapping("/webhook/payment-status")
    public ResponseEntity<ApiResponse<OrderResponse>> updatePaymentStatus(
            @RequestBody PaymentStatusUpdateRequest request) {

        log.info("Received payment status update for order: {} - Status: {}",
                request.getOrderId(), request.getPaymentStatus());

        OrderResponse response = orderService.updatePaymentStatus(
                request.getOrderId(),
                request.getPaymentStatus(),
                request.getRazorpayPaymentId()
        );

        return ResponseEntity.ok(ApiResponse.success("Payment status updated successfully", response));
    }
}
