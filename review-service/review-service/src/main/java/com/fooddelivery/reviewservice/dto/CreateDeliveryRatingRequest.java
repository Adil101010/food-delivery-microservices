package com.fooddelivery.reviewservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDeliveryRatingRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Delivery partner ID is required")
    private Long deliveryPartnerId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    private String comment;

    private Double tipAmount;

    @Min(value = 1, message = "Punctuality rating must be at least 1")
    @Max(value = 5, message = "Punctuality rating must be at most 5")
    private Integer punctualityRating;

    @Min(value = 1, message = "Behaviour rating must be at least 1")
    @Max(value = 5, message = "Behaviour rating must be at most 5")
    private Integer behaviourRating;

    @Min(value = 1, message = "Packaging rating must be at least 1")
    @Max(value = 5, message = "Packaging rating must be at most 5")
    private Integer packagingRating;
}
