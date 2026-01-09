package com.fooddelivery.analyticsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {

    private Long totalOrders;
    private Long totalUsers;
    private Long totalRestaurants;
    private Long totalDeliveries;

    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private BigDecimal monthRevenue;

    private Long pendingOrders;
    private Long completedOrders;
    private Long cancelledOrders;

    private BigDecimal averageOrderValue;
    private Double averageRating;

    private Long activeDeliveryPartners;
    private Long activeRestaurants;
}
