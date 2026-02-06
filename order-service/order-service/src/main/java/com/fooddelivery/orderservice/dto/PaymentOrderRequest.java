package com.fooddelivery.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderRequest {
    private Long orderId;
    private Long userId;
    private Double amount;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
}
