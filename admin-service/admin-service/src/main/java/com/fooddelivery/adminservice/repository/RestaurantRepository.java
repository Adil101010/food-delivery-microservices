package com.fooddelivery.adminservice.repository;

import com.fooddelivery.adminservice.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByStatus(String status);

    List<Restaurant> findByIsActive(Boolean isActive);

    @Query("SELECT COUNT(r) FROM Restaurant r WHERE r.status = 'PENDING'")
    Long countPendingApprovals();

    @Query("SELECT r FROM Restaurant r ORDER BY r.createdAt DESC")
    List<Restaurant> findAllOrderByCreatedAtDesc();
}
