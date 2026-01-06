package com.fooddelivery.orderservice.dto;

import com.fooddelivery.orderservice.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;

    @NotNull(message = "Restaurant name is required")
    private String restaurantName;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;

    @NotNull(message = "Delivery fee is required")
    private Double deliveryFee;

    private Double discount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Delivery address is required")
    private String deliveryAddress;

    private String deliveryInstructions;

    @NotNull(message = "Customer phone is required")
    private String customerPhone;

    private String customerName;
}
