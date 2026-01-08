package com.fooddelivery.locationservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLocationRequest {

    @NotNull(message = "Partner ID is required")
    private Long partnerId;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private Double speed;

    private Double heading;

    private Double accuracy;

    private Boolean isMoving;

    private Boolean isOnline;

    private Long currentDeliveryId;
}
