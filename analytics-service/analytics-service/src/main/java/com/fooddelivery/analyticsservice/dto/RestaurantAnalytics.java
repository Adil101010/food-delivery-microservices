package com.fooddelivery.analyticsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantAnalytics {

    private Long restaurantId;
    private String restaurantName;
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private Double averageRating;
    private Long totalReviews;
    private String status;
}
