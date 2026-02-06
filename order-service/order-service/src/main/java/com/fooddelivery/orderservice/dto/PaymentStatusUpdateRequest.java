package com.fooddelivery.orderservice.dto;

import com.fooddelivery.orderservice.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusUpdateRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Payment status is required")
    private PaymentStatus paymentStatus;

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private String razorpaySignature;
}
