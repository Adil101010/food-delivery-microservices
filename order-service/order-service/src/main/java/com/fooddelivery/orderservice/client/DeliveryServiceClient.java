package com.fooddelivery.orderservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class DeliveryServiceClient {

    private final RestTemplate restTemplate;

    @Value("${delivery.service.url:http://localhost:8086}")
    private String deliveryServiceUrl;

    @Value("${internal.secret}")
    private String internalSecret;

    //  Constructor injection — Spring manage karega
    public DeliveryServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void createPendingDelivery(Long orderId) {
        try {
            String url = deliveryServiceUrl +
                    "/api/deliveries/order/" + orderId + "/create-pending";

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Secret", internalSecret);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            log.info("Calling delivery-service: {} with secret: {}",
                    url, internalSecret != null ? "SET" : "NULL"); //  Debug log

            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
            log.info(" Delivery created for order: {}", orderId);

        } catch (Exception e) {
            log.warn(" Delivery create failed for order {}: {}",
                    orderId, e.getMessage());
        }
    }
}

