package com.fooddelivery.analyticsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAnalytics {

    private Long totalUsers;
    private Long activeUsers;
    private Long newUsersToday;
    private Long newUsersThisWeek;
    private Long newUsersThisMonth;

    private Long topCustomerId;
    private String topCustomerName;
    private Long topCustomerOrders;
    private BigDecimal topCustomerSpending;
}
