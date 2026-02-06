package com.fooddelivery.orderservice.client;

import com.fooddelivery.orderservice.dto.PaymentOrderRequest;
import com.fooddelivery.orderservice.dto.PaymentOrderResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    public PaymentOrderResponse createPaymentOrder(PaymentOrderRequest request) {
        String url = paymentServiceUrl + "/api/payments/razorpay/create-order";

        log.info("Calling Payment Service: {}", url);
        log.info("Request: orderId={}, amount={}", request.getOrderId(), request.getAmount());

        try {
            // Call Payment Service
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            log.info("Payment Service raw response: {}", response);

            if (response != null && response.containsKey("data")) {
                // Extract data from ApiResponse wrapper
                Map<String, Object> data = (Map<String, Object>) response.get("data");

                // Convert to PaymentOrderResponse
                PaymentOrderResponse paymentResponse = PaymentOrderResponse.builder()
                        .razorpayOrderId((String) data.get("razorpayOrderId"))
                        .orderId(request.getOrderId())
                        .amount(request.getAmount())
                        .currency((String) data.get("currency"))
                        .status((String) data.get("status"))
                        .keyId((String) data.get("keyId"))
                        .build();

                log.info("Payment order created: {}", paymentResponse.getRazorpayOrderId());
                return paymentResponse;
            }

            log.error("Invalid response from Payment Service");
            return null;

        } catch (Exception e) {
            log.error("Error calling Payment Service: {}", e.getMessage(), e);
            return null;
        }
    }
}
