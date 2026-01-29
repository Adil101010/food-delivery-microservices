package com.fooddelivery.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVerificationResponse {

    private Boolean verified;
    private String message;
    private String paymentId;
    private String orderId;
    private String status;
    private Double amount;
    private String transactionId;
}
