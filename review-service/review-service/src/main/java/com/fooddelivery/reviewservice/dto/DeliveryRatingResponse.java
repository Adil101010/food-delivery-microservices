package com.fooddelivery.reviewservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRatingResponse {

    private Long id;
    private Long orderId;
    private Long userId;
    private Long deliveryPartnerId;
    private Integer rating;
    private String comment;
    private Double tipAmount;
    private Integer punctualityRating;
    private Integer behaviourRating;
    private Integer packagingRating;
    private LocalDateTime createdAt;
}
