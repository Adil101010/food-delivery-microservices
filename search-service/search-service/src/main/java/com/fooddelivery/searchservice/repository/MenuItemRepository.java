package com.fooddelivery.searchservice.repository;

import com.fooddelivery.searchservice.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    // Search menu items by name
    @Query("SELECT m FROM MenuItem m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) AND m.isAvailable = true")
    List<MenuItem> searchByName(@Param("query") String query);

    // Search by category
    List<MenuItem> findByCategoryAndIsAvailable(String category, Boolean isAvailable);

    // Search by restaurant
    List<MenuItem> findByRestaurantIdAndIsAvailable(Long restaurantId, Boolean isAvailable);

    // Search with price range
    @Query("SELECT m FROM MenuItem m WHERE m.price BETWEEN :minPrice AND :maxPrice AND m.isAvailable = true")
    List<MenuItem> searchByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                      @Param("maxPrice") BigDecimal maxPrice);

    // Advanced menu search
    @Query("SELECT m FROM MenuItem m WHERE " +
            "(LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) OR :query IS NULL) AND " +
            "(m.category = :category OR :category IS NULL) AND " +
            "(m.price >= :minPrice OR :minPrice IS NULL) AND " +
            "(m.price <= :maxPrice OR :maxPrice IS NULL) AND " +
            "m.isAvailable = true")
    List<MenuItem> advancedSearch(@Param("query") String query,
                                  @Param("category") String category,
                                  @Param("minPrice") BigDecimal minPrice,
                                  @Param("maxPrice") BigDecimal maxPrice);

    // Get popular items
    @Query("SELECT m FROM MenuItem m WHERE m.isAvailable = true ORDER BY m.restaurantId")
    List<MenuItem> findPopularItems();

    // ✅ FIXED: Auto-complete for menu items (wildcard both sides)
    @Query("SELECT DISTINCT m.name FROM MenuItem m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) AND m.isAvailable = true")
    List<String> getAutoCompleteSuggestions(@Param("query") String query);
}
