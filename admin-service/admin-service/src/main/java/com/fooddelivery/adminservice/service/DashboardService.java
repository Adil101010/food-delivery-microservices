package com.fooddelivery.adminservice.service;

import com.fooddelivery.adminservice.dto.DashboardStatsDTO;
import com.fooddelivery.adminservice.repository.OrderRepository;
import com.fooddelivery.adminservice.repository.RestaurantRepository;
import com.fooddelivery.adminservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;

    public DashboardStatsDTO getDashboardStats() {
        // Get today's start time
        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);

        // Total counts
        Long totalUsers = userRepository.count();
        Long totalRestaurants = restaurantRepository.count();
        Long totalOrders = orderRepository.count();

        // Count delivery partners (users with role DELIVERY_PARTNER)
        Long totalDeliveryPartners = (long) userRepository.findByRole("DELIVERY_PARTNER").size();

        // Order stats
        Long activeOrders = orderRepository.countActiveOrders();
        Long completedOrders = orderRepository.countCompletedOrders();
        Long cancelledOrders = orderRepository.countCancelledOrders();

        // Revenue stats
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        BigDecimal todayRevenue = orderRepository.calculateRevenueByDate(todayStart);
        if (todayRevenue == null) todayRevenue = BigDecimal.ZERO;

        Long todayOrders = orderRepository.countOrdersByDate(todayStart);

        // Pending approvals
        Long pendingRestaurantApprovals = restaurantRepository.countPendingApprovals();

        // Support tickets (set to 0 for now, will implement later)
        Long pendingSupportTickets = 0L;

        return DashboardStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalRestaurants(totalRestaurants)
                .totalOrders(totalOrders)
                .totalDeliveryPartners(totalDeliveryPartners)
                .activeOrders(activeOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .totalRevenue(totalRevenue)
                .todayRevenue(todayRevenue)
                .todayOrders(todayOrders)
                .pendingRestaurantApprovals(pendingRestaurantApprovals)
                .pendingSupportTickets(pendingSupportTickets)
                .build();
    }
}
