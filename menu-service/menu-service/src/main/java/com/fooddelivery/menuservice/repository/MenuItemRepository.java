package com.fooddelivery.menuservice.repository;

import com.fooddelivery.menuservice.entity.Category;
import com.fooddelivery.menuservice.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByRestaurantId(Long restaurantId);

    List<MenuItem> findByRestaurantIdAndIsAvailableTrue(Long restaurantId);

    List<MenuItem> findByRestaurantIdAndCategory(Long restaurantId, Category category);

    List<MenuItem> findByRestaurantIdAndIsVegetarianTrue(Long restaurantId);

    List<MenuItem> findByRestaurantIdAndIsBestsellerTrue(Long restaurantId);
}
