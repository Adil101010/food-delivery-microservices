package com.fooddelivery.orderservice.controller;

import com.fooddelivery.orderservice.dto.CreateOrderRequest;
import com.fooddelivery.orderservice.dto.MessageResponse;
import com.fooddelivery.orderservice.dto.OrderResponse;
import com.fooddelivery.orderservice.enums.OrderStatus;
import com.fooddelivery.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    // Health Check
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        return ResponseEntity.ok(new MessageResponse("Order Service is running"));
    }

    // Create Order
    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    // Get Order by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        OrderResponse order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    // Get User Orders
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getUserOrders(@PathVariable Long userId) {
        List<OrderResponse> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    // Get Restaurant Orders
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<OrderResponse>> getRestaurantOrders(@PathVariable Long restaurantId) {
        List<OrderResponse> orders = orderService.getRestaurantOrders(restaurantId);
        return ResponseEntity.ok(orders);
    }

    // Update Order Status
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        OrderResponse order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(order);
    }

    // Cancel Order
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<MessageResponse> cancelOrder(@PathVariable Long orderId) {
        MessageResponse response = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(response);
    }

    // Get Orders by Status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<OrderResponse> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }
}
