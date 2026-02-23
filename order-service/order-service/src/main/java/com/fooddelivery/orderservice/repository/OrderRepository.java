package com.fooddelivery.orderservice.repository;

import com.fooddelivery.orderservice.entity.Order;
import com.fooddelivery.orderservice.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Pagination
    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByRestaurantId(Long restaurantId, Pageable pageable);

    Page<Order> findByOrderStatus(OrderStatus orderStatus, Pageable pageable);

    Page<Order> findByUserIdAndOrderStatus(Long userId, OrderStatus orderStatus, Pageable pageable);

    Page<Order> findByRestaurantIdAndOrderStatus(Long restaurantId, OrderStatus orderStatus, Pageable pageable);
}