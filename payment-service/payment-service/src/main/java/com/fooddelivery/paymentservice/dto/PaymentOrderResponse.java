package com.fooddelivery.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentOptions {
        private String key;
        private String orderId;
        private Double amount;
        private String currency;
        private String name;
        private String description;
        private String image;
        private PrefillData prefill;
        private String callbackUrl;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class PrefillData {
            private String name;
            private String email;
            private String contact;
        }
    }
}
