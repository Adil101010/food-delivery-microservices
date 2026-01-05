package com.fooddelivery.menuservice.service;

import com.fooddelivery.menuservice.dto.MenuItemRequest;
import com.fooddelivery.menuservice.dto.MenuItemResponse;
import com.fooddelivery.menuservice.entity.Category;
import com.fooddelivery.menuservice.entity.MenuItem;
import com.fooddelivery.menuservice.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuItemService {

    @Autowired
    private MenuItemRepository menuItemRepository;

    public MenuItemResponse addMenuItem(MenuItemRequest request) {
        MenuItem menuItem = new MenuItem();
        menuItem.setRestaurantId(request.getRestaurantId());
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setCategory(request.getCategory());
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setIsVegetarian(request.getIsVegetarian() != null ? request.getIsVegetarian() : false);
        menuItem.setIsVegan(request.getIsVegan() != null ? request.getIsVegan() : false);
        menuItem.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);
        menuItem.setIngredients(request.getIngredients());
        menuItem.setAllergens(request.getAllergens());
        menuItem.setPreparationTime(request.getPreparationTime() != null ? request.getPreparationTime() : 15);
        menuItem.setCalories(request.getCalories());
        menuItem.setIsBestseller(request.getIsBestseller() != null ? request.getIsBestseller() : false);
        menuItem.setIsSpicy(request.getIsSpicy() != null ? request.getIsSpicy() : false);
        menuItem.setSpiceLevel(request.getSpiceLevel() != null ? request.getSpiceLevel() : 0);

        MenuItem savedItem = menuItemRepository.save(menuItem);
        return mapToResponse(savedItem);
    }

    public MenuItemResponse getMenuItemById(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + id));
        return mapToResponse(menuItem);
    }

    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest request) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + id));

        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setCategory(request.getCategory());
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setIsVegetarian(request.getIsVegetarian());
        menuItem.setIsVegan(request.getIsVegan());
        menuItem.setIsAvailable(request.getIsAvailable());
        menuItem.setIngredients(request.getIngredients());
        menuItem.setAllergens(request.getAllergens());
        menuItem.setPreparationTime(request.getPreparationTime());
        menuItem.setCalories(request.getCalories());
        menuItem.setIsBestseller(request.getIsBestseller());
        menuItem.setIsSpicy(request.getIsSpicy());
        menuItem.setSpiceLevel(request.getSpiceLevel());

        MenuItem updatedItem = menuItemRepository.save(menuItem);
        return mapToResponse(updatedItem);
    }

    public void deleteMenuItem(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + id));
        menuItemRepository.delete(menuItem);
    }

    public List<MenuItemResponse> getMenuItemsByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MenuItemResponse> getAvailableMenuItems(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MenuItemResponse> getMenuItemsByCategory(Long restaurantId, Category category) {
        return menuItemRepository.findByRestaurantIdAndCategory(restaurantId, category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public MenuItemResponse toggleAvailability(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + id));

        menuItem.setIsAvailable(!menuItem.getIsAvailable());
        MenuItem updatedItem = menuItemRepository.save(menuItem);
        return mapToResponse(updatedItem);
    }

    private MenuItemResponse mapToResponse(MenuItem menuItem) {
        return new MenuItemResponse(
                menuItem.getId(),
                menuItem.getRestaurantId(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getCategory(),
                menuItem.getImageUrl(),
                menuItem.getIsVegetarian(),
                menuItem.getIsVegan(),
                menuItem.getIsAvailable(),
                menuItem.getRating(),
                menuItem.getTotalOrders(),
                menuItem.getIngredients(),
                menuItem.getAllergens(),
                menuItem.getPreparationTime(),
                menuItem.getCalories(),
                menuItem.getIsBestseller(),
                menuItem.getIsSpicy(),
                menuItem.getSpiceLevel(),
                menuItem.getCreatedAt(),
                menuItem.getUpdatedAt()
        );
    }
}
