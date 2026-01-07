package com.fooddelivery.deliveryservice.dto;

import com.fooddelivery.deliveryservice.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponse {

    private Long id;
    private Long orderId;
    private Long partnerId;
    private Long restaurantId;
    private Long customerId;
    private DeliveryStatus status;
    private String pickupAddress;
    private String deliveryAddress;
    private Double deliveryFee;
    private Double distance;
    private String customerPhone;
    private String deliveryInstructions;
    private LocalDateTime assignedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private Integer estimatedTime;
    private String rejectionReason;
    private Double partnerEarning;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
