package com.fooddelivery.assignmentservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualAssignRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Partner ID is required")
    private Long partnerId;

    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Restaurant latitude is required")
    private Double restaurantLatitude;

    @NotNull(message = "Restaurant longitude is required")
    private Double restaurantLongitude;

    @NotNull(message = "Customer latitude is required")
    private Double customerLatitude;

    @NotNull(message = "Customer longitude is required")
    private Double customerLongitude;
}
