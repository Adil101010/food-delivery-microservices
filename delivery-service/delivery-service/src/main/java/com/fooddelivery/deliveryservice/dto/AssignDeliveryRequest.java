package com.fooddelivery.deliveryservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignDeliveryRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Partner ID is required")
    private Long partnerId;

    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Pickup address is required")
    private String pickupAddress;

    @NotNull(message = "Delivery address is required")
    private String deliveryAddress;

    @NotNull(message = "Delivery fee is required")
    private Double deliveryFee;

    private Double distance;

    private String customerPhone;

    private String deliveryInstructions;

    private Integer estimatedTime;
}
