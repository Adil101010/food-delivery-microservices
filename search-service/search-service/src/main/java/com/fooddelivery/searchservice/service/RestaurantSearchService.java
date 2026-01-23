package com.fooddelivery.searchservice.service;

import com.fooddelivery.searchservice.dto.RestaurantSearchDTO;
import com.fooddelivery.searchservice.dto.SearchFilters;
import com.fooddelivery.searchservice.entity.Restaurant;
import com.fooddelivery.searchservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantSearchService {

    private final RestaurantRepository restaurantRepository;

    // ✅ ADDED CACHE
    @Cacheable(value = "restaurants", key = "#query", unless = "#result.isEmpty()")
    public List<RestaurantSearchDTO> searchByName(String query) {
        log.info("Fetching restaurants from database for query: {}", query);
        List<Restaurant> restaurants = restaurantRepository.searchByName(query);
        return restaurants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ ADDED CACHE
    @Cacheable(value = "restaurants", key = "'cuisine:' + #cuisine", unless = "#result.isEmpty()")
    public List<RestaurantSearchDTO> searchByCuisine(String cuisine) {
        log.info("Fetching restaurants from database for cuisine: {}", cuisine);
        List<Restaurant> restaurants = restaurantRepository.searchByCuisine(cuisine);
        return restaurants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ ADDED CACHE
    @Cacheable(value = "restaurants", key = "'rating:' + #minRating", unless = "#result.isEmpty()")
    public List<RestaurantSearchDTO> getTopRated(Double minRating) {
        log.info("Fetching top rated restaurants from database: {}", minRating);
        List<Restaurant> restaurants = restaurantRepository.searchByRating(minRating);
        return restaurants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ ADDED CACHE
    @Cacheable(value = "restaurants", key = "'all'", unless = "#result.isEmpty()")
    public List<RestaurantSearchDTO> getAllByRating() {
        log.info("Fetching all restaurants from database");
        List<Restaurant> restaurants = restaurantRepository.findAllActiveOrderByRating();
        return restaurants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Advanced search - cache with complex key
    @Cacheable(value = "searchResults",
            key = "'adv:' + #filters.query + ':' + #filters.cuisine + ':' + #filters.minRating + ':' + #filters.maxDeliveryTime",
            unless = "#result.isEmpty()")
    public List<RestaurantSearchDTO> advancedSearch(SearchFilters filters) {
        log.info("Fetching advanced search results from database: {}", filters);
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

    // Auto-complete - short TTL
    @Cacheable(value = "searchResults", key = "'autocomplete:' + #query")
    public List<String> getAutoCompleteSuggestions(String query) {
        log.info("Fetching autocomplete suggestions from database: {}", query);
        return restaurantRepository.getAutoCompleteSuggestions(query);
    }

    // ✅ ADDED CACHE EVICTION (for admin updates)
    @CacheEvict(value = "restaurants", allEntries = true)
    public void clearRestaurantCache() {
        log.info("Clearing restaurant cache");
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
            return true;
        }

        try {
            LocalTime now = LocalTime.now();
            LocalTime open = LocalTime.parse(openingTime);
            LocalTime close = LocalTime.parse(closingTime);

            return now.isAfter(open) && now.isBefore(close);
        } catch (Exception e) {
            return true;
        }
    }
}
