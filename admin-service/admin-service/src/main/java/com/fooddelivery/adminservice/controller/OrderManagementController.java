package com.fooddelivery.adminservice.controller;

import com.fooddelivery.adminservice.dto.OrderListDTO;
import com.fooddelivery.adminservice.entity.Order;
import com.fooddelivery.adminservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderManagementController {

    private final OrderRepository orderRepository;

    // Get All Orders
    @GetMapping
    public ResponseEntity<List<OrderListDTO>> getAllOrders() {
        List<Order> orders = orderRepository.findAllOrderByCreatedAtDesc();
        List<OrderListDTO> orderDTOs = orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDTOs);
    }

    // Get Orders by Status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderListDTO>> getOrdersByStatus(@PathVariable String status) {
        List<Order> orders = orderRepository.findByStatus(status);
        List<OrderListDTO> orderDTOs = orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDTOs);
    }

    // Get Order by ID
    @GetMapping("/{id}")
    public ResponseEntity<OrderListDTO> getOrderById(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return ResponseEntity.ok(convertToDTO(order));
    }

    // Helper method to convert Order to DTO
    private OrderListDTO convertToDTO(Order order) {
        return OrderListDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .userName("User-" + order.getUserId()) // Simplified
                .restaurantId(order.getRestaurantId())
                .restaurantName("Restaurant-" + order.getRestaurantId()) // Simplified
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
