package com.fooddelivery.analyticsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderAnalytics {

    private Long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;

    private Map<String, Long> ordersByStatus;
    private Map<String, Long> ordersByType;
    private Map<String, Long> ordersByPaymentMethod;

    private Long todayOrders;
    private Long weekOrders;
    private Long monthOrders;
}
