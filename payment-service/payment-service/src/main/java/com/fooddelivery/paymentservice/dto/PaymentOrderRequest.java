package com.fooddelivery.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    private Long userId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    private String currency = "INR";

    private String customerName;

    private String customerEmail;

    private String customerPhone;
}
