package com.fooddelivery.searchservice.service;

import com.fooddelivery.searchservice.dto.RestaurantSearchDTO;
import com.fooddelivery.searchservice.dto.SearchFilters;
import com.fooddelivery.searchservice.entity.Restaurant;
import com.fooddelivery.searchservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantSearchService {

    private final RestaurantRepository restaurantRepository;

    // Simple search by name
    public List<RestaurantSearchDTO> searchByName(String query) {
        List<Restaurant> restaurants = restaurantRepository.searchByName(query);
        return restaurants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Search by cuisine
    public List<RestaurantSearchDTO> searchByCuisine(String cuisine) {
        List<Restaurant> restaurants = restaurantRepository.searchByCuisine(cuisine);
        return restaurants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Advanced search with filters
    public List<RestaurantSearchDTO> advancedSearch(SearchFilters filters) {
        List<Restaurant> restaurants = restaurantRepository.advancedSearch(
                filters.getQuery(),
                filters.getCuisine(),
                filters.getMinRating(),
                filters.getMaxDeliveryTime()
        );

        return restaurants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get all restaurants sorted by rating
    public List<RestaurantSearchDTO> getAllByRating() {
        List<Restaurant> restaurants = restaurantRepository.findAllActiveOrderByRating();
        return restaurants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get top rated restaurants
    public List<RestaurantSearchDTO> getTopRated(Double minRating) {
        List<Restaurant> restaurants = restaurantRepository.searchByRating(minRating);
        return restaurants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Auto-complete suggestions
    public List<String> getAutoCompleteSuggestions(String query) {
        return restaurantRepository.getAutoCompleteSuggestions(query);
    }

    // Convert Restaurant entity to DTO
    private RestaurantSearchDTO convertToDTO(Restaurant restaurant) {
        boolean isOpen = checkIfOpen(restaurant.getOpeningTime(), restaurant.getClosingTime());

        return RestaurantSearchDTO.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .cuisine(restaurant.getCuisine())
                .address(restaurant.getAddress())
                .rating(restaurant.getRating())
                .deliveryTime(restaurant.getDeliveryTime())
                .isActive(restaurant.getIsActive())
                .isOpen(isOpen)
                .phone(restaurant.getPhone())
                .email(restaurant.getEmail())
                .build();
    }

    // Helper method to check if restaurant is open
    private boolean checkIfOpen(String openingTime, String closingTime) {
        if (openingTime == null || closingTime == null) {
            return true; // Assume open if times not set
        }

        try {
            LocalTime now = LocalTime.now();
            LocalTime open = LocalTime.parse(openingTime);
            LocalTime close = LocalTime.parse(closingTime);

            return now.isAfter(open) && now.isBefore(close);
        } catch (Exception e) {
            return true; // Default to open if parsing fails
        }
    }
}
