package com.fooddelivery.paymentservice.dto;

import com.fooddelivery.paymentservice.enums.PaymentMethod;
import com.fooddelivery.paymentservice.enums.PaymentStatus;
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
public class PaymentResponse {

    private Long id;
    private Long paymentId;  // ADD THIS (alias for id)
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;

    // ADD THIS
    private String paymentDescription;

    // Razorpay fields
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    // ADD GATEWAY FIELDS
    private String gatewayOrderId;
    private String gatewayPaymentId;
    private String gatewaySignature;

    // Customer details
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    // Transaction details
    private String transactionId;
    private String failureReason;

    // ADD REFUND FIELDS
    private BigDecimal refundAmount;
    private String refundTransactionId;
    private LocalDateTime refundedAt;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paidAt;
    private LocalDateTime completedAt;

    //  ADD MESSAGE AND TIMESTAMP (for API responses)
    private String message;
    private LocalDateTime timestamp;
}
