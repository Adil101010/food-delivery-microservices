package com.fooddelivery.reviewservice.repository;

import com.fooddelivery.reviewservice.entity.Review;
import com.fooddelivery.reviewservice.enums.ReviewType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByOrderId(Long orderId);

    List<Review> findByRestaurantId(Long restaurantId);

    List<Review> findByUserId(Long userId);

    List<Review> findByRestaurantIdAndType(Long restaurantId, ReviewType type);

    List<Review> findByMenuItemId(Long menuItemId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.restaurantId = :restaurantId AND r.type = :type")
    Double getAverageRating(@Param("restaurantId") Long restaurantId, @Param("type") ReviewType type);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.restaurantId = :restaurantId")
    Long getReviewCount(@Param("restaurantId") Long restaurantId);
}
