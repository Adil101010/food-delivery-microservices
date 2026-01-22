package com.fooddelivery.adminservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {
    private Long totalUsers;
    private Long totalRestaurants;
    private Long totalOrders;
    private Long totalDeliveryPartners;
    private Long activeOrders;
    private Long completedOrders;
    private Long cancelledOrders;
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private Long todayOrders;
    private Long pendingRestaurantApprovals;
    private Long pendingSupportTickets;
}
