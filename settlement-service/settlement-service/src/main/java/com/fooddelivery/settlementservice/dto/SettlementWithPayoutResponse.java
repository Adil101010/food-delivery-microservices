package com.fooddelivery.settlementservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettlementWithPayoutResponse {
    private SettlementResponse settlement;
    private PayoutResponse payout;
}
