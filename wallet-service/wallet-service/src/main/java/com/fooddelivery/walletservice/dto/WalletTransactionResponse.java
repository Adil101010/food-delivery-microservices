package com.fooddelivery.walletservice.dto;

import com.fooddelivery.walletservice.enums.TransactionType;
import com.fooddelivery.walletservice.enums.WalletTransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionResponse {
    private Long id;
    private Long walletId;
    private BigDecimal amount;
    private TransactionType type;
    private WalletTransactionType transactionType;
    private String description;
    private Long orderId;
    private String referenceId;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private LocalDateTime createdAt;
}
