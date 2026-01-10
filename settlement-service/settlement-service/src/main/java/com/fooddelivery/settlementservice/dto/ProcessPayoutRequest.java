package com.fooddelivery.settlementservice.dto;

import com.fooddelivery.settlementservice.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPayoutRequest {

    @NotNull(message = "Settlement ID is required")
    private Long settlementId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Account details are required")
    private String accountDetails;
}
