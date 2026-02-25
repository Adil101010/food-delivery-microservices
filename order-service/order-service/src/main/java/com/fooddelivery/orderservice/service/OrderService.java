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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;
    private final DeliveryServiceClient deliveryServiceClient;

    // ─────────────────────────────────────────
    // VALID STATUS TRANSITIONS MAP
    // ─────────────────────────────────────────
    private static final Map<OrderStatus, List<OrderStatus>> VALID_TRANSITIONS = Map.of(
            OrderStatus.CONFIRMED,        List.of(OrderStatus.PREPARING),
            OrderStatus.PREPARING,        List.of(OrderStatus.OUT_FOR_DELIVERY),
            OrderStatus.OUT_FOR_DELIVERY, List.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED,        List.of(),
            OrderStatus.CANCELLED,        List.of(),
            OrderStatus.PENDING_PAYMENT,  List.of()
    );

    // ─────────────────────────────────────────
    // CREATE ORDER
    // ─────────────────────────────────────────
    @Transactional
    public OrderWithPaymentResponse createOrder(CreateOrderRequest request) {

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
        order.setOrderStatus(OrderStatus.PENDING_PAYMENT);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setDeliveryInstructions(request.getDeliveryInstructions());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(45));

        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItemId(itemRequest.getMenuItemId());
            orderItem.setItemName(itemRequest.getItemName());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(itemRequest.getPrice());
            orderItem.setSubtotal(itemRequest.getPrice() * itemRequest.getQuantity());
            orderItem.setSpecialInstructions(itemRequest.getSpecialInstructions());
            orderItem.setOrderStatus(OrderStatus.PENDING_PAYMENT);
            orderItem.setPaymentStatus(PaymentStatus.PENDING);
            orderItem.setPaymentMethod(request.getPaymentMethod().name());
            order.addItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order {} saved with PENDING_PAYMENT status", savedOrder.getId());

        PaymentOrderResponse paymentResponse;
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
            savedOrder.setRazorpayOrderId(paymentResponse.getRazorpayOrderId());
            orderRepository.save(savedOrder);
            log.info("Razorpay order created: {}", paymentResponse.getRazorpayOrderId());

        } catch (Exception e) {
            log.error("Payment initialization failed: {}", e.getMessage());
            savedOrder.setOrderStatus(OrderStatus.CANCELLED);
            orderRepository.save(savedOrder);
            throw new RuntimeException("Payment initialization failed. Please try again.");
        }

        return OrderWithPaymentResponse.builder()
                .order(convertToResponse(savedOrder))
                .payment(paymentResponse)
                .build();
    }

    // ─────────────────────────────────────────
    // GET ORDER BY ID
    // ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        return convertToResponse(order);
    }

    // ─────────────────────────────────────────
    // GET USER ORDERS
    // ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
                .map(this::convertToResponse);
    }

    // ─────────────────────────────────────────
    // GET RESTAURANT ORDERS
    // ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<OrderResponse> getRestaurantOrders(Long restaurantId, Pageable pageable) {
        return orderRepository.findByRestaurantId(restaurantId, pageable)
                .map(this::convertToResponse);
    }

    // ─────────────────────────────────────────
    // GET ORDERS BY STATUS
    // ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByOrderStatus(status, pageable)
                .map(this::convertToResponse);
    }

    // ─────────────────────────────────────────
    // UPDATE ORDER STATUS — Fix 3: Transition Restriction
    // ─────────────────────────────────────────
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        OrderStatus current = order.getOrderStatus();

        // ✅ Fix 3 — Valid transition check
        List<OrderStatus> allowed = VALID_TRANSITIONS.getOrDefault(current, List.of());
        if (!allowed.contains(newStatus)) {
            throw new RuntimeException(
                    "Invalid status transition: " + current + " → " + newStatus);
        }

        order.setOrderStatus(newStatus);

        if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        return convertToResponse(orderRepository.save(order));
    }

    // ─────────────────────────────────────────
    // UPDATE PAYMENT STATUS — Fix 1: Duplicate Webhook Safe
    // ─────────────────────────────────────────
    @Transactional
    public OrderResponse updatePaymentStatus(Long orderId,
                                             PaymentStatus paymentStatus,
                                             String razorpayPaymentId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // ✅ Fix 1 — Duplicate webhook protection
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            log.warn("Order {} already PAID — duplicate webhook ignored", orderId);
            return convertToResponse(order);
        }

        order.setPaymentStatus(paymentStatus);
        order.setRazorpayPaymentId(razorpayPaymentId);

        if (paymentStatus == PaymentStatus.PAID) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
            log.info("Order {} CONFIRMED after payment success", orderId);

            try {
                deliveryServiceClient.createPendingDelivery(orderId);
                log.info("Delivery created for order {}", orderId);
            } catch (Exception e) {
                log.warn("Delivery creation failed for order {}: {}", orderId, e.getMessage());
            }

        } else if (paymentStatus == PaymentStatus.FAILED) {
            order.setOrderStatus(OrderStatus.CANCELLED);
            log.info("Order {} CANCELLED due to payment failure", orderId);
        }

        return convertToResponse(orderRepository.save(order));
    }

    // ─────────────────────────────────────────
    // CANCEL ORDER
    // ─────────────────────────────────────────
    @Transactional
    public MessageResponse cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel a delivered order");
        }

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order is already cancelled");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        return new MessageResponse("Order cancelled successfully");
    }

    // ─────────────────────────────────────────
    // CONVERT TO RESPONSE
    // ─────────────────────────────────────────
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

        response.setItems(
                order.getItems().stream()
                        .map(item -> new OrderItemResponse(
                                item.getId(),
                                item.getMenuItemId(),
                                item.getItemName(),
                                item.getQuantity(),
                                item.getPrice(),
                                item.getSubtotal(),
                                item.getSpecialInstructions()
                        ))
                        .collect(Collectors.toList())
        );

        return response;
    }
}
