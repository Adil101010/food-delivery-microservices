package com.fooddelivery.settlementservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettlementSummaryResponse {
    private Long entityId;
    private Integer totalSettlements;
    private BigDecimal totalGrossAmount;
    private BigDecimal totalCommission;
    private BigDecimal totalNetAmount;
    private Integer pendingSettlements;
    private Integer completedSettlements;
}
