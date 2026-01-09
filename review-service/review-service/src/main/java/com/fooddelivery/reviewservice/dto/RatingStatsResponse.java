package com.fooddelivery.reviewservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingStatsResponse {

    private Long entityId; // Restaurant or Partner ID
    private String entityType; // "RESTAURANT" or "PARTNER"
    private Double averageRating;
    private Long totalReviews;
    private String message;
}
