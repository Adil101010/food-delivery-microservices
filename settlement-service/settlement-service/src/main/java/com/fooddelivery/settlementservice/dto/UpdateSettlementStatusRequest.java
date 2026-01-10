package com.fooddelivery.settlementservice.dto;

import com.fooddelivery.settlementservice.enums.SettlementStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSettlementStatusRequest {

    @NotNull(message = "Status is required")
    private SettlementStatus status;
}
