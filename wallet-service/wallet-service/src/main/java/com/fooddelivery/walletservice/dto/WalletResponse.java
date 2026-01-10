package com.fooddelivery.walletservice.dto;

import com.fooddelivery.walletservice.enums.WalletStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {
    private Long id;
    private Long userId;
    private BigDecimal balance;
    private WalletStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
