package com.fooddelivery.orderservice.service;

import com.fooddelivery.orderservice.dto.*;
import com.fooddelivery.orderservice.entity.Order;
import com.fooddelivery.orderservice.entity.OrderItem;
import com.fooddelivery.orderservice.enums.OrderStatus;
import com.fooddelivery.orderservice.enums.PaymentStatus;
import com.fooddelivery.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    // Create Order
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        // Calculate amounts
        double subtotal = request.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        double tax = subtotal * 0.05; // 5% tax
        double discount = request.getDiscount() != null ? request.getDiscount() : 0.0;
        double totalAmount = subtotal + request.getDeliveryFee() + tax - discount;

        // Create Order Entity
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setRestaurantId(request.getRestaurantId());
        order.setRestaurantName(request.getRestaurantName());
        order.setSubtotal(subtotal);
        order.setDeliveryFee(request.getDeliveryFee());
        order.setTax(tax);
        order.setDiscount(discount);
        order.setTotalAmount(totalAmount);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setDeliveryInstructions(request.getDeliveryInstructions());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setCustomerName(request.getCustomerName());
        order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(45)); // 45 min estimate

        // Add Order Items
        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItemId(itemRequest.getMenuItemId());
            orderItem.setItemName(itemRequest.getItemName());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(itemRequest.getPrice());
            orderItem.setSubtotal(itemRequest.getPrice() * itemRequest.getQuantity());
            orderItem.setSpecialInstructions(itemRequest.getSpecialInstructions());
            order.addItem(orderItem);
        }

        // Save Order
        Order savedOrder = orderRepository.save(order);

        return convertToResponse(savedOrder);
    }

    // Get Order by ID
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        return convertToResponse(order);
    }

    // Get User Orders
    public List<OrderResponse> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get Restaurant Orders
    public List<OrderResponse> getRestaurantOrders(Long restaurantId) {
        List<Order> orders = orderRepository.findByRestaurantId(restaurantId);
        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Update Order Status
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        order.setOrderStatus(newStatus);

        // If delivered, set delivered time
        if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        Order updatedOrder = orderRepository.save(order);
        return convertToResponse(updatedOrder);
    }

    // Cancel Order
    @Transactional
    public MessageResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // Can only cancel if not delivered
        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel delivered order");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        return new MessageResponse("Order cancelled successfully");
    }

    // Get Orders by Status
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByOrderStatus(status);
        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Helper Method: Convert Entity to Response DTO
    private OrderResponse convertToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setRestaurantId(order.getRestaurantId());
        response.setRestaurantName(order.getRestaurantName());
        response.setSubtotal(order.getSubtotal());
        response.setDeliveryFee(order.getDeliveryFee());
        response.setTax(order.getTax());
        response.setDiscount(order.getDiscount());
        response.setTotalAmount(order.getTotalAmount());
        response.setOrderStatus(order.getOrderStatus());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setDeliveryInstructions(order.getDeliveryInstructions());
        response.setCustomerPhone(order.getCustomerPhone());
        response.setCustomerName(order.getCustomerName());
        response.setEstimatedDeliveryTime(order.getEstimatedDeliveryTime());
        response.setDeliveredAt(order.getDeliveredAt());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        // Convert OrderItems
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getMenuItemId(),
                        item.getItemName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getSubtotal(),
                        item.getSpecialInstructions()
                ))
                .collect(Collectors.toList());

        response.setItems(itemResponses);

        return response;
    }
}
