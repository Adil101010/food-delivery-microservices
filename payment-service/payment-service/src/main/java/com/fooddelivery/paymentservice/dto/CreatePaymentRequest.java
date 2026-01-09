package com.fooddelivery.paymentservice.dto;

import com.fooddelivery.paymentservice.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Amount is required")
    private Double amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String paymentDescription;
}
