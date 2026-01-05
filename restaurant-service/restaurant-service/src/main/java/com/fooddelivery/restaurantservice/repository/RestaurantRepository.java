package com.fooddelivery.restaurantservice.repository;

import com.fooddelivery.restaurantservice.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByCity(String city);

    List<Restaurant> findByCityAndIsActiveTrue(String city);

    List<Restaurant> findByCuisineContaining(String cuisine);

    Optional<Restaurant> findByOwnerId(Long ownerId);

    List<Restaurant> findByIsActiveTrueAndIsOpenTrue();
}
