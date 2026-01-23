package com.fooddelivery.searchservice.service;

import com.fooddelivery.searchservice.dto.MenuItemSearchDTO;
import com.fooddelivery.searchservice.dto.SearchFilters;
import com.fooddelivery.searchservice.entity.MenuItem;
import com.fooddelivery.searchservice.entity.Restaurant;
import com.fooddelivery.searchservice.repository.MenuItemRepository;
import com.fooddelivery.searchservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuSearchService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    // ✅ ADDED CACHE
    @Cacheable(value = "menuItems", key = "'search:' + #query", unless = "#result.isEmpty()")
    public List<MenuItemSearchDTO> searchByName(String query) {
        log.info("Fetching menu items from database for query: {}", query);
        List<MenuItem> menuItems = menuItemRepository.searchByName(query);
        return menuItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ ADDED CACHE
    @Cacheable(value = "menuItems", key = "'category:' + #category", unless = "#result.isEmpty()")
    public List<MenuItemSearchDTO> searchByCategory(String category) {
        log.info("Fetching menu items from database for category: {}", category);
        List<MenuItem> menuItems = menuItemRepository.findByCategoryAndIsAvailable(category, true);
        return menuItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ ADDED CACHE
    @Cacheable(value = "menuItems", key = "'restaurant:' + #restaurantId", unless = "#result.isEmpty()")
    public List<MenuItemSearchDTO> searchByRestaurant(Long restaurantId) {
        log.info("Fetching menu items from database for restaurant: {}", restaurantId);
        List<MenuItem> menuItems = menuItemRepository.findByRestaurantIdAndIsAvailable(restaurantId, true);
        return menuItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ ADDED CACHE
    @Cacheable(value = "menuItems",
            key = "'price:' + #filters.minPrice + ':' + #filters.maxPrice",
            unless = "#result.isEmpty()")
    public List<MenuItemSearchDTO> searchByPriceRange(SearchFilters filters) {
        log.info("Fetching menu items from database for price range: {} - {}",
                filters.getMinPrice(), filters.getMaxPrice());
        List<MenuItem> menuItems = menuItemRepository.searchByPriceRange(
                filters.getMinPrice(),
                filters.getMaxPrice()
        );
        return menuItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Advanced menu search with cache
    @Cacheable(value = "searchResults",
            key = "'menu:' + #filters.query + ':' + #filters.category + ':' + #filters.minPrice + ':' + #filters.maxPrice",
            unless = "#result.isEmpty()")
    public List<MenuItemSearchDTO> advancedSearch(SearchFilters filters) {
        log.info("Fetching advanced menu search from database: {}", filters);
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

    // ✅ ADDED CACHE - Popular items (long TTL)
    @Cacheable(value = "popularItems", key = "'all'")
    public List<MenuItemSearchDTO> getPopularItems() {
        log.info("Fetching popular items from database");
        List<MenuItem> menuItems = menuItemRepository.findPopularItems();
        return menuItems.stream()
                .limit(20)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Auto-complete
    @Cacheable(value = "searchResults", key = "'menu-autocomplete:' + #query")
    public List<String> getAutoCompleteSuggestions(String query) {
        log.info("Fetching menu autocomplete from database: {}", query);
        return menuItemRepository.getAutoCompleteSuggestions(query);
    }

    // ✅ ADDED CACHE EVICTION
    @CacheEvict(value = "menuItems", allEntries = true)
    public void clearMenuCache() {
        log.info("Clearing menu cache");
    }

    // Convert MenuItem entity to DTO
    private MenuItemSearchDTO convertToDTO(MenuItem menuItem) {
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
