package com.fooddelivery.searchservice.controller;

import com.fooddelivery.searchservice.service.MenuSearchService;
import com.fooddelivery.searchservice.service.RestaurantSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/search/cache")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CacheController {

    private final CacheManager cacheManager;
    private final RestaurantSearchService restaurantSearchService;
    private final MenuSearchService menuSearchService;

    // Get cache statistics
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        cacheManager.getCacheNames().forEach(cacheName -> {
            stats.put(cacheName, "Active");
        });

        stats.put("totalCaches", cacheManager.getCacheNames().size());
        stats.put("status", "Redis cache enabled");

        return ResponseEntity.ok(stats);
    }

    // Clear all caches
    @PostMapping("/clear")
    public ResponseEntity<Map<String, String>> clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });

        Map<String, String> response = new HashMap<>();
        response.put("message", "All caches cleared successfully");
        return ResponseEntity.ok(response);
    }

    // Clear restaurant cache
    @PostMapping("/clear/restaurants")
    public ResponseEntity<Map<String, String>> clearRestaurantCache() {
        restaurantSearchService.clearRestaurantCache();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Restaurant cache cleared");
        return ResponseEntity.ok(response);
    }

    // Clear menu cache
    @PostMapping("/clear/menu")
    public ResponseEntity<Map<String, String>> clearMenuCache() {
        menuSearchService.clearMenuCache();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Menu cache cleared");
        return ResponseEntity.ok(response);
    }
}
