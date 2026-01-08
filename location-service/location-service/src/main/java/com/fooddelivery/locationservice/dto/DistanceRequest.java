package com.fooddelivery.locationservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistanceRequest {

    @NotNull(message = "Start latitude is required")
    private Double startLatitude;

    @NotNull(message = "Start longitude is required")
    private Double startLongitude;

    @NotNull(message = "End latitude is required")
    private Double endLatitude;

    @NotNull(message = "End longitude is required")
    private Double endLongitude;
}
