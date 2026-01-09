package com.fooddelivery.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPaymentRequest {

    @NotNull(message = "Payment ID is required")
    private Long paymentId;

    @NotNull(message = "Gateway order ID is required")
    private String gatewayOrderId;

    @NotNull(message = "Gateway payment ID is required")
    private String gatewayPaymentId;

    @NotNull(message = "Gateway signature is required")
    private String gatewaySignature;
}
