package com.fooddelivery.adminservice.service;

import com.fooddelivery.adminservice.dto.RestaurantListDTO;
import com.fooddelivery.adminservice.entity.Restaurant;
import com.fooddelivery.adminservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantManagementService {

    private final RestaurantRepository restaurantRepository;

    public List<RestaurantListDTO> getAllRestaurants() {
        return restaurantRepository.findAllOrderByCreatedAtDesc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<RestaurantListDTO> getPendingRestaurants() {
        return restaurantRepository.findByStatus("PENDING").stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public RestaurantListDTO getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
        return convertToDTO(restaurant);
    }

    public void approveRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
        restaurant.setStatus("APPROVED");
        restaurant.setIsActive(true);
        restaurant.setUpdatedAt(LocalDateTime.now());
        restaurantRepository.save(restaurant);
    }

    public void rejectRestaurant(Long id, String reason) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
        restaurant.setStatus("REJECTED");
        restaurant.setIsActive(false);
        restaurant.setUpdatedAt(LocalDateTime.now());
        restaurantRepository.save(restaurant);
    }

    public void blockRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
        restaurant.setStatus("BLOCKED");
        restaurant.setIsActive(false);
        restaurant.setUpdatedAt(LocalDateTime.now());
        restaurantRepository.save(restaurant);
    }

    public void unblockRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
        restaurant.setStatus("APPROVED");
        restaurant.setIsActive(true);
        restaurant.setUpdatedAt(LocalDateTime.now());
        restaurantRepository.save(restaurant);
    }

    private RestaurantListDTO convertToDTO(Restaurant restaurant) {
        Long totalOrders = 0L; // Simplified for now

        return RestaurantListDTO.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .phone(restaurant.getPhone())
                .email(restaurant.getEmail())
                .cuisine(restaurant.getCuisine())
                .status(restaurant.getStatus())
                .isActive(restaurant.getIsActive())
                .rating(restaurant.getRating())
                .createdAt(restaurant.getCreatedAt())
                .totalOrders(totalOrders)
                .build();
    }
}
