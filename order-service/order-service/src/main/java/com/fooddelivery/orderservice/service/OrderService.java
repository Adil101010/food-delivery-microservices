package com.fooddelivery.orderservice.service;

import com.fooddelivery.orderservice.client.DeliveryServiceClient;
import com.fooddelivery.orderservice.client.PaymentClient;
import com.fooddelivery.orderservice.dto.*;
import com.fooddelivery.orderservice.entity.Order;
import com.fooddelivery.orderservice.entity.OrderItem;
import com.fooddelivery.orderservice.enums.OrderStatus;
import com.fooddelivery.orderservice.enums.PaymentStatus;
import com.fooddelivery.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;
    private final DeliveryServiceClient deliveryServiceClient;

    @Transactional
    public OrderWithPaymentResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for user: {}", request.getUserId());

        double subtotal = request.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        double tax = subtotal * 0.05;
        double discount = request.getDiscount() != null ? request.getDiscount() : 0.0;
        double totalAmount = subtotal + request.getDeliveryFee() + tax - discount;

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
        order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(45));

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

        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {}", savedOrder.getId());

        // ✅ Auto-create PENDING delivery
        try {
            deliveryServiceClient.createPendingDelivery(savedOrder.getId());
            log.info("✅ Delivery record created for order: {}", savedOrder.getId());
        } catch (Exception e) {
            log.warn("⚠️ Could not create delivery for order {}: {}",
                    savedOrder.getId(), e.getMessage());
        }

        // Create Payment Order
        PaymentOrderResponse paymentResponse = null;
        try {
            PaymentOrderRequest paymentRequest = PaymentOrderRequest.builder()
                    .orderId(savedOrder.getId())
                    .userId(savedOrder.getUserId())
                    .amount(savedOrder.getTotalAmount())
                    .customerName(savedOrder.getCustomerName())
                    .customerEmail(request.getCustomerEmail())
                    .customerPhone(savedOrder.getCustomerPhone())
                    .build();

            paymentResponse = paymentClient.createPaymentOrder(paymentRequest);
            log.info("Payment order created: {}", paymentResponse.getRazorpayOrderId());

            savedOrder.setRazorpayOrderId(paymentResponse.getRazorpayOrderId());
            orderRepository.save(savedOrder);

        } catch (Exception e) {
            log.error("Failed to create payment order: {}", e.getMessage(), e);
        }

        return OrderWithPaymentResponse.builder()
                .order(convertToResponse(savedOrder))
                .payment(paymentResponse)
                .build();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        log.info("Fetching order with ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        return convertToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(Long userId) {
        log.info("Fetching orders for user: {}", userId);
        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getRestaurantOrders(Long restaurantId) {
        log.info("Fetching orders for restaurant: {}", restaurantId);
        return orderRepository.findByRestaurantId(restaurantId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        log.info("Updating order {} status to: {}", orderId, newStatus);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        order.setOrderStatus(newStatus);
        if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
            order.setPaymentStatus(PaymentStatus.PAID);
        }
        return convertToResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updatePaymentStatus(Long orderId, PaymentStatus paymentStatus,
                                             String razorpayPaymentId) {
        log.info("Updating payment status for order: {} to {}", orderId, paymentStatus);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        order.setPaymentStatus(paymentStatus);
        order.setRazorpayPaymentId(razorpayPaymentId);

        if (paymentStatus == PaymentStatus.PAID) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
            log.info("Order {} confirmed after successful payment", orderId);
        } else if (paymentStatus == PaymentStatus.FAILED) {
            order.setOrderStatus(OrderStatus.CANCELLED);
            log.warn("Order {} cancelled due to payment failure", orderId);
        }

        return convertToResponse(orderRepository.save(order));
    }

    @Transactional
    public MessageResponse cancelOrder(Long orderId) {
        log.info("Cancelling order: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel delivered order");
        }
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        return new MessageResponse("Order cancelled successfully");
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        log.info("Fetching orders with status: {}", status);
        return orderRepository.findByOrderStatus(status)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

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
