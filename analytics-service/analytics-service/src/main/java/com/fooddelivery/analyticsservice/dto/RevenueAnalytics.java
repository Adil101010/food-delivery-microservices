package com.fooddelivery.analyticsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueAnalytics {

    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private BigDecimal weekRevenue;
    private BigDecimal monthRevenue;

    private Map<String, BigDecimal> revenueByPaymentMethod;

    private BigDecimal foodRevenue;
    private BigDecimal deliveryRevenue;
    private BigDecimal discountGiven;

    private BigDecimal averageDailyRevenue;
    private BigDecimal highestOrderValue;
    private BigDecimal lowestOrderValue;
}
