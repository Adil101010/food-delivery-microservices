package com.fooddelivery.locationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponse {

    private Long id;
    private Long partnerId;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double heading;
    private Double accuracy;
    private Boolean isMoving;
    private Boolean isOnline;
    private Long currentDeliveryId;
    private LocalDateTime updatedAt;
}
