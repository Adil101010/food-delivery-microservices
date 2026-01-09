package com.fooddelivery.paymentservice.dto;

import com.fooddelivery.paymentservice.enums.PaymentMethod;
import com.fooddelivery.paymentservice.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private Long orderId;
    private Long userId;
    private Double amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionId;
    private String gatewayOrderId;
    private String gatewayPaymentId;
    private String paymentDescription;
    private String failureReason;
    private Double refundAmount;
    private String refundTransactionId;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
