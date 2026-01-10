package com.fooddelivery.settlementservice.dto;

import com.fooddelivery.settlementservice.enums.PayoutStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePayoutStatusRequest {

    @NotNull(message = "Status is required")
    private PayoutStatus status;

    private String transactionId;
}
