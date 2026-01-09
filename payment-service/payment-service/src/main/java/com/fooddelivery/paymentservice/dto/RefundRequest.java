package com.fooddelivery.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {

    @NotNull(message = "Payment ID is required")
    private Long paymentId;

    private Double refundAmount; // Null = full refund

    private String reason;
}
