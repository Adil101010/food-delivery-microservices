package com.fooddelivery.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentOrderResponse {
    private String razorpayOrderId;
    private Long orderId;
    private Double amount;
    private String currency;
    private String status;
    private String keyId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private PaymentOptions paymentOptions;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentOptions {
        private String key;
        private String orderId;
        private Integer amount;
        private String currency;
        private String name;
        private String description;
        private String image;
        private PrefillData prefill;
        private String callbackUrl;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrefillData {
        private String name;
        private String email;
        private String contact;
    }
}
