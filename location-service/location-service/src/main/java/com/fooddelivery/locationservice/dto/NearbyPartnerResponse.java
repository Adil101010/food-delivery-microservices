package com.fooddelivery.locationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearbyPartnerResponse {

    private Long partnerId;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;
    private Integer etaMinutes;
    private Boolean isMoving;
    private Long currentDeliveryId;
}
