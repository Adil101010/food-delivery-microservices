package com.fooddelivery.orderservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class DeliveryServiceClient {

    private final RestTemplate restTemplate;

    @Value("${delivery.service.url:http://localhost:8082}")
    private String deliveryServiceUrl;

    public DeliveryServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    public void createPendingDelivery(Long orderId) {
        try {
            String url = deliveryServiceUrl +
                    "/api/deliveries/order/" + orderId + "/create-pending";
            restTemplate.postForEntity(url, null, Void.class);
            log.info("✅ Delivery created for order: {}", orderId);
        } catch (Exception e) {
            log.warn("⚠️ Delivery create failed for order {}: {}",
                    orderId, e.getMessage());
        }
    }
}
