package com.fooddelivery.orderservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.orderservice.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class RazorpayWebhookService {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;


    public boolean verifyPaymentSignature(String razorpayOrderId,
                                          String razorpayPaymentId,
                                          String razorpaySignature) {
        try {
            String payload = razorpayOrderId + "|" + razorpayPaymentId;
            String generatedSignature = generateHmac(payload, razorpayKeySecret);
            boolean isValid = generatedSignature.equals(razorpaySignature);
            log.info("Payment signature valid: {}", isValid);
            return isValid;
        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }


    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            String generatedSignature = generateHmac(payload, webhookSecret);
            boolean isValid = generatedSignature.equals(signature);
            log.info("Webhook signature valid: {}", isValid);
            return isValid;
        } catch (Exception e) {
            log.error("Webhook signature error: {}", e.getMessage());
            return false;
        }
    }


    public void processWebhook(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String event = root.path("event").asText();
            log.info("Webhook event: {}", event);

            if ("payment.captured".equals(event)) {
                // Payment success
                JsonNode paymentNode = root.path("payload").path("payment").path("entity");
                String razorpayPaymentId = paymentNode.path("id").asText();
                String razorpayOrderId = paymentNode.path("order_id").asText();


                Long orderId = paymentNode.path("notes").path("orderId").asLong();

                if (orderId > 0) {
                    orderService.updatePaymentStatus(orderId, PaymentStatus.PAID, razorpayPaymentId);
                    log.info("Order {} confirmed via webhook", orderId);
                }

            } else if ("payment.failed".equals(event)) {

                JsonNode paymentNode = root.path("payload").path("payment").path("entity");
                Long orderId = paymentNode.path("notes").path("orderId").asLong();

                if (orderId > 0) {
                    orderService.updatePaymentStatus(orderId, PaymentStatus.FAILED, null);
                    log.info("Order {} cancelled via webhook", orderId);
                }
            }

        } catch (Exception e) {
            log.error("Webhook processing error: {}", e.getMessage());
        }
    }

    // HMAC-SHA256 generate
    private String generateHmac(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKey);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}
