package com.fooddelivery.settlementservice.dto;

import com.fooddelivery.settlementservice.enums.EntityType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSettlementRequest {

    @NotNull(message = "Entity type is required")
    private EntityType entityType;

    @NotNull(message = "Entity ID is required")
    private Long entityId;

    @NotNull(message = "Period start date is required")
    private LocalDate periodStart;

    @NotNull(message = "Period end date is required")
    private LocalDate periodEnd;

    @NotNull(message = "Total orders is required")
    private Integer totalOrders;

    @NotNull(message = "Gross amount is required")
    @DecimalMin(value = "0.0", message = "Gross amount must be positive")
    private BigDecimal grossAmount;

    @NotNull(message = "Commission rate is required")
    @DecimalMin(value = "0.0", message = "Commission rate must be positive")
    private BigDecimal commissionRate;
}
