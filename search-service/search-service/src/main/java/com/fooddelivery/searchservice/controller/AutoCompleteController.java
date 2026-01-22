package com.fooddelivery.searchservice.controller;

import com.fooddelivery.searchservice.service.MenuSearchService;
import com.fooddelivery.searchservice.service.RestaurantSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search/autocomplete")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AutoCompleteController {

    private final RestaurantSearchService restaurantSearchService;
    private final MenuSearchService menuSearchService;

    // Auto-complete for restaurants
    @GetMapping("/restaurants")
    public ResponseEntity<List<String>> restaurantAutoComplete(
            @RequestParam String query) {

        if (query == null || query.trim().length() < 2) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        List<String> suggestions = restaurantSearchService.getAutoCompleteSuggestions(query);
        return ResponseEntity.ok(suggestions);
    }

    // Auto-complete for menu items
    @GetMapping("/menu")
    public ResponseEntity<List<String>> menuAutoComplete(
            @RequestParam String query) {

        if (query == null || query.trim().length() < 2) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        List<String> suggestions = menuSearchService.getAutoCompleteSuggestions(query);
        return ResponseEntity.ok(suggestions);
    }

    // Combined auto-complete (restaurants + menu items)
    @GetMapping("/all")
    public ResponseEntity<Map<String, List<String>>> combinedAutoComplete(
            @RequestParam String query) {

        Map<String, List<String>> results = new HashMap<>();

        if (query == null || query.trim().length() < 2) {
            results.put("restaurants", new ArrayList<>());
            results.put("menuItems", new ArrayList<>());
            return ResponseEntity.ok(results);
        }

        List<String> restaurantSuggestions = restaurantSearchService.getAutoCompleteSuggestions(query);
        List<String> menuSuggestions = menuSearchService.getAutoCompleteSuggestions(query);

        results.put("restaurants", restaurantSuggestions);
        results.put("menuItems", menuSuggestions);

        return ResponseEntity.ok(results);
    }
}
