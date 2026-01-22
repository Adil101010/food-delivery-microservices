package com.fooddelivery.searchservice.service;

import com.fooddelivery.searchservice.dto.MenuItemSearchDTO;
import com.fooddelivery.searchservice.dto.SearchFilters;
import com.fooddelivery.searchservice.entity.MenuItem;
import com.fooddelivery.searchservice.entity.Restaurant;
import com.fooddelivery.searchservice.repository.MenuItemRepository;
import com.fooddelivery.searchservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuSearchService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    // Simple search by name
    public List<MenuItemSearchDTO> searchByName(String query) {
        List<MenuItem> menuItems = menuItemRepository.searchByName(query);
        return menuItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Search by category
    public List<MenuItemSearchDTO> searchByCategory(String category) {
        List<MenuItem> menuItems = menuItemRepository.findByCategoryAndIsAvailable(category, true);
        return menuItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Search by restaurant
    public List<MenuItemSearchDTO> searchByRestaurant(Long restaurantId) {
        List<MenuItem> menuItems = menuItemRepository.findByRestaurantIdAndIsAvailable(restaurantId, true);
        return menuItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Search by price range
    public List<MenuItemSearchDTO> searchByPriceRange(SearchFilters filters) {
        List<MenuItem> menuItems = menuItemRepository.searchByPriceRange(
                filters.getMinPrice(),
                filters.getMaxPrice()
        );
        return menuItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Advanced menu search
    public List<MenuItemSearchDTO> advancedSearch(SearchFilters filters) {
        List<MenuItem> menuItems = menuItemRepository.advancedSearch(
                filters.getQuery(),
                filters.getCategory(),
                filters.getMinPrice(),
                filters.getMaxPrice()
        );
        return menuItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get popular items
    public List<MenuItemSearchDTO> getPopularItems() {
        List<MenuItem> menuItems = menuItemRepository.findPopularItems();
        return menuItems.stream()
                .limit(20) // Limit to top 20
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Auto-complete suggestions
    public List<String> getAutoCompleteSuggestions(String query) {
        return menuItemRepository.getAutoCompleteSuggestions(query);
    }

    // Convert MenuItem entity to DTO
    private MenuItemSearchDTO convertToDTO(MenuItem menuItem) {
        // Get restaurant name
        String restaurantName = restaurantRepository.findById(menuItem.getRestaurantId())
                .map(Restaurant::getName)
                .orElse("Unknown Restaurant");

        return MenuItemSearchDTO.builder()
                .id(menuItem.getId())
                .name(menuItem.getName())
                .description(menuItem.getDescription())
                .price(menuItem.getPrice())
                .category(menuItem.getCategory())
                .isAvailable(menuItem.getIsAvailable())
                .restaurantId(menuItem.getRestaurantId())
                .restaurantName(restaurantName)
                .imageUrl(menuItem.getImageUrl())
                .build();
    }
}
