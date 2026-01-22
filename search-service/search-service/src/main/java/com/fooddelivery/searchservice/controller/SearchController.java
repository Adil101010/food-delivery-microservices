package com.fooddelivery.searchservice.controller;

import com.fooddelivery.searchservice.dto.MessageResponse;
import com.fooddelivery.searchservice.dto.MenuItemSearchDTO;
import com.fooddelivery.searchservice.dto.RestaurantSearchDTO;
import com.fooddelivery.searchservice.dto.SearchFilters;
import com.fooddelivery.searchservice.service.MenuSearchService;
import com.fooddelivery.searchservice.service.RestaurantSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SearchController {

    private final RestaurantSearchService restaurantSearchService;
    private final MenuSearchService menuSearchService;

    // Health Check
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        return ResponseEntity.ok(new MessageResponse("Search Service is running"));
    }

    // ==================== RESTAURANT SEARCH ====================

    // Search restaurants by name
    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantSearchDTO>> searchRestaurants(
            @RequestParam(required = false) String query) {

        if (query == null || query.trim().isEmpty()) {
            // Return all restaurants sorted by rating
            List<RestaurantSearchDTO> restaurants = restaurantSearchService.getAllByRating();
            return ResponseEntity.ok(restaurants);
        }

        List<RestaurantSearchDTO> restaurants = restaurantSearchService.searchByName(query);
        return ResponseEntity.ok(restaurants);
    }

    // Search restaurants by cuisine
    @GetMapping("/restaurants/cuisine/{cuisine}")
    public ResponseEntity<List<RestaurantSearchDTO>> searchByCuisine(
            @PathVariable String cuisine) {
        List<RestaurantSearchDTO> restaurants = restaurantSearchService.searchByCuisine(cuisine);
        return ResponseEntity.ok(restaurants);
    }

    // Get top-rated restaurants
    @GetMapping("/restaurants/top-rated")
    public ResponseEntity<List<RestaurantSearchDTO>> getTopRatedRestaurants(
            @RequestParam(defaultValue = "4.0") Double minRating) {
        List<RestaurantSearchDTO> restaurants = restaurantSearchService.getTopRated(minRating);
        return ResponseEntity.ok(restaurants);
    }

    // Advanced restaurant search with filters
    @PostMapping("/restaurants/advanced")
    public ResponseEntity<List<RestaurantSearchDTO>> advancedRestaurantSearch(
            @RequestBody SearchFilters filters) {
        List<RestaurantSearchDTO> restaurants = restaurantSearchService.advancedSearch(filters);
        return ResponseEntity.ok(restaurants);
    }

    // ==================== MENU ITEM SEARCH ====================

    // Search menu items by name
    @GetMapping("/menu")
    public ResponseEntity<List<MenuItemSearchDTO>> searchMenuItems(
            @RequestParam(required = false) String query) {

        if (query == null || query.trim().isEmpty()) {
            // Return popular items
            List<MenuItemSearchDTO> menuItems = menuSearchService.getPopularItems();
            return ResponseEntity.ok(menuItems);
        }

        List<MenuItemSearchDTO> menuItems = menuSearchService.searchByName(query);
        return ResponseEntity.ok(menuItems);
    }

    // Search menu items by category
    @GetMapping("/menu/category/{category}")
    public ResponseEntity<List<MenuItemSearchDTO>> searchByCategory(
            @PathVariable String category) {
        List<MenuItemSearchDTO> menuItems = menuSearchService.searchByCategory(category);
        return ResponseEntity.ok(menuItems);
    }

    // Search menu items by restaurant
    @GetMapping("/menu/restaurant/{restaurantId}")
    public ResponseEntity<List<MenuItemSearchDTO>> searchByRestaurant(
            @PathVariable Long restaurantId) {
        List<MenuItemSearchDTO> menuItems = menuSearchService.searchByRestaurant(restaurantId);
        return ResponseEntity.ok(menuItems);
    }

    // Get popular menu items
    @GetMapping("/menu/popular")
    public ResponseEntity<List<MenuItemSearchDTO>> getPopularItems() {
        List<MenuItemSearchDTO> menuItems = menuSearchService.getPopularItems();
        return ResponseEntity.ok(menuItems);
    }

    // Advanced menu search with filters
    @PostMapping("/menu/advanced")
    public ResponseEntity<List<MenuItemSearchDTO>> advancedMenuSearch(
            @RequestBody SearchFilters filters) {
        List<MenuItemSearchDTO> menuItems = menuSearchService.advancedSearch(filters);
        return ResponseEntity.ok(menuItems);
    }
}
