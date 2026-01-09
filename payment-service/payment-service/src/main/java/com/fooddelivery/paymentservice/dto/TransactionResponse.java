package com.fooddelivery.paymentservice.dto;

import com.fooddelivery.paymentservice.enums.PaymentStatus;
import com.fooddelivery.paymentservice.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private Long paymentId;
    private Long orderId;
    private TransactionType type;
    private PaymentStatus status;
    private Double amount;
    private String transactionId;
    private String message;
    private LocalDateTime timestamp;
}
