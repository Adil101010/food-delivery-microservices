package com.fooddelivery.searchservice.repository;

import com.fooddelivery.searchservice.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // Search by name
    @Query("SELECT r FROM Restaurant r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) AND r.isActive = true")
    List<Restaurant> searchByName(@Param("query") String query);

    // Search by cuisine
    @Query("SELECT r FROM Restaurant r WHERE LOWER(r.cuisine) LIKE LOWER(CONCAT('%', :cuisine, '%')) AND r.isActive = true")
    List<Restaurant> searchByCuisine(@Param("cuisine") String cuisine);

    // Search with rating filter
    @Query("SELECT r FROM Restaurant r WHERE r.rating >= :minRating AND r.isActive = true ORDER BY r.rating DESC")
    List<Restaurant> searchByRating(@Param("minRating") Double minRating);

    // Advanced search with multiple filters
    @Query("SELECT r FROM Restaurant r WHERE " +
            "(LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) OR :query IS NULL) AND " +
            "(LOWER(r.cuisine) LIKE LOWER(CONCAT('%', :cuisine, '%')) OR :cuisine IS NULL) AND " +
            "(r.rating >= :minRating OR :minRating IS NULL) AND " +
            "(r.deliveryTime <= :maxDeliveryTime OR :maxDeliveryTime IS NULL) AND " +
            "r.isActive = true")
    List<Restaurant> advancedSearch(@Param("query") String query,
                                    @Param("cuisine") String cuisine,
                                    @Param("minRating") Double minRating,
                                    @Param("maxDeliveryTime") Integer maxDeliveryTime);

    // Get all active restaurants sorted by rating
    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true ORDER BY r.rating DESC")
    List<Restaurant> findAllActiveOrderByRating();

    // ✅ FIXED: Auto-complete suggestions (wildcard both sides)
    @Query("SELECT DISTINCT r.name FROM Restaurant r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) AND r.isActive = true")
    List<String> getAutoCompleteSuggestions(@Param("query") String query);
}
