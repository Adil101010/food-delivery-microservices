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

public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // Health Check
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        return ResponseEntity.ok(new MessageResponse("Analytics Service is running"));
    }
    // AnalyticsController.java mein add karo
    @GetMapping("/restaurant/{restaurantId}/weekly-revenue")
    public ResponseEntity<?> getWeeklyRevenue(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(analyticsService.getRestaurantWeeklyRevenue(restaurantId));
    }

    // Dashboard Stats
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        DashboardStats stats = analyticsService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
    // =============================================
// RESTAURANT SPECIFIC ENDPOINTS — ADD THESE
// =============================================

    @GetMapping("/restaurant/{restaurantId}/revenue")
    public ResponseEntity<?> getRestaurantRevenue(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(analyticsService.getRestaurantRevenue(restaurantId, from, to));
    }

    @GetMapping("/restaurant/{restaurantId}/orders")
    public ResponseEntity<?> getRestaurantOrderStats(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(analyticsService.getRestaurantOrderStats(restaurantId));
    }

    @GetMapping("/restaurant/{restaurantId}/top-items")
    public ResponseEntity<?> getRestaurantTopItems(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(analyticsService.getRestaurantTopItems(restaurantId, limit));
    }

    @GetMapping("/restaurant/{restaurantId}/recent-orders")
    public ResponseEntity<?> getRestaurantRecentOrders(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getRestaurantRecentOrders(restaurantId, limit));
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
