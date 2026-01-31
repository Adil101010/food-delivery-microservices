package com.fooddelivery.paymentservice.dto;

import com.fooddelivery.paymentservice.enums.PaymentStatus;
import com.fooddelivery.paymentservice.enums.TransactionType;
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
public class TransactionResponse {

    private Long id;
    private Long paymentId;
    private TransactionType type;
    private BigDecimal amount;  //  BigDecimal, not Double
    private PaymentStatus status;
    private String gatewayTransactionId;
    private String description;
    private LocalDateTime createdAt;

    //  ADD THESE
    private String transactionId;
    private String message;
    private LocalDateTime timestamp;
}
