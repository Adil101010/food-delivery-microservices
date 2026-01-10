package com.fooddelivery.settlementservice.dto;

import com.fooddelivery.settlementservice.enums.EntityType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculateSettlementRequest {

    @NotNull(message = "Entity type is required")
    private EntityType entityType;

    @NotNull(message = "Entity ID is required")
    private Long entityId;

    @NotNull(message = "Period start date is required")
    private LocalDate periodStart;

    @NotNull(message = "Period end date is required")
    private LocalDate periodEnd;
}
