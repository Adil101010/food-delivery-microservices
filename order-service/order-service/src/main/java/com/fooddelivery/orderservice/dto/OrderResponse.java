package com.fooddelivery.orderservice.dto;

import com.fooddelivery.orderservice.enums.OrderStatus;
import com.fooddelivery.orderservice.enums.PaymentMethod;
import com.fooddelivery.orderservice.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private Long userId;
    private Long restaurantId;
    private String restaurantName;
    private List<OrderItemResponse> items;
    private Double subtotal;
    private Double deliveryFee;
    private Double tax;
    private Double discount;
    private Double totalAmount;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private String deliveryAddress;
    private String deliveryInstructions;
    private String customerPhone;
    private String customerName;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
