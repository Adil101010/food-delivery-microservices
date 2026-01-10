package com.fooddelivery.settlementservice.dto;

import com.fooddelivery.settlementservice.enums.PaymentMethod;
import com.fooddelivery.settlementservice.enums.PayoutStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayoutResponse {
    private Long id;
    private Long settlementId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private String accountDetails;
    private String transactionId;
    private PayoutStatus status;
    private LocalDateTime initiatedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
