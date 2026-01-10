package com.fooddelivery.settlementservice.dto;

import com.fooddelivery.settlementservice.enums.EntityType;
import com.fooddelivery.settlementservice.enums.SettlementStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettlementResponse {
    private Long id;
    private EntityType entityType;
    private Long entityId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Integer totalOrders;
    private BigDecimal grossAmount;
    private BigDecimal commissionRate;
    private BigDecimal commissionAmount;
    private BigDecimal netAmount;
    private SettlementStatus status;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
