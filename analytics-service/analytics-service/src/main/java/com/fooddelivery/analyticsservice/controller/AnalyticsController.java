package com.fooddelivery.analyticsservice.controller;

import com.fooddelivery.analyticsservice.dto.*;
import com.fooddelivery.analyticsservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // Health Check
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        return ResponseEntity.ok(new MessageResponse("Analytics Service is running"));
    }

    // Dashboard Stats
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        DashboardStats stats = analyticsService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    // Order Analytics
    @GetMapping("/orders")
    public ResponseEntity<OrderAnalytics> getOrderAnalytics() {
        OrderAnalytics analytics = analyticsService.getOrderAnalytics();
        return ResponseEntity.ok(analytics);
    }

    // Restaurant Analytics
    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantAnalytics>> getRestaurantAnalytics() {
        List<RestaurantAnalytics> analytics = analyticsService.getRestaurantAnalytics();
        return ResponseEntity.ok(analytics);
    }

    // User Analytics
    @GetMapping("/users")
    public ResponseEntity<UserAnalytics> getUserAnalytics() {
        UserAnalytics analytics = analyticsService.getUserAnalytics();
        return ResponseEntity.ok(analytics);
    }

    // Delivery Analytics
    @GetMapping("/deliveries")
    public ResponseEntity<DeliveryAnalytics> getDeliveryAnalytics() {
        DeliveryAnalytics analytics = analyticsService.getDeliveryAnalytics();
        return ResponseEntity.ok(analytics);
    }

    // Revenue Analytics
    @GetMapping("/revenue")
    public ResponseEntity<RevenueAnalytics> getRevenueAnalytics() {
        RevenueAnalytics analytics = analyticsService.getRevenueAnalytics();
        return ResponseEntity.ok(analytics);
    }
}
