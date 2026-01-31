package com.fooddelivery.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentVerificationResponse {
    private Boolean verified;
    private String message;
    private String paymentId;
    private String orderId;
    private String status;
    private BigDecimal amount;
    private String transactionId;
    private LocalDateTime paidAt;
}
