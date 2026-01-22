package com.fooddelivery.adminservice.repository;

import com.fooddelivery.adminservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(String status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status IN ('PENDING', 'CONFIRMED', 'PREPARING', 'OUT_FOR_DELIVERY')")
    Long countActiveOrders();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'DELIVERED'")
    Long countCompletedOrders();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'CANCELLED'")
    Long countCancelledOrders();

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED'")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt >= :startDate")
    BigDecimal calculateRevenueByDate(LocalDateTime startDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startDate")
    Long countOrdersByDate(LocalDateTime startDate);

    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findAllOrderByCreatedAtDesc();
}
