package com.fooddelivery.menuservice.controller;

import com.fooddelivery.menuservice.dto.MenuItemRequest;
import com.fooddelivery.menuservice.dto.MenuItemResponse;
import com.fooddelivery.menuservice.dto.MessageResponse;
import com.fooddelivery.menuservice.entity.Category;
import com.fooddelivery.menuservice.service.MenuItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(origins = "*")
public class MenuItemController {

    @Autowired
    private MenuItemService menuItemService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "menu-service");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    public ResponseEntity<?> addMenuItem(@Valid @RequestBody MenuItemRequest request) {
        try {
            MenuItemResponse response = menuItemService.addMenuItem(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<?> getMenuItemById(@PathVariable Long id) {
        try {
            MenuItemResponse response = menuItemService.getMenuItemById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<?> updateMenuItem(@PathVariable Long id, @Valid @RequestBody MenuItemRequest request) {
        try {
            MenuItemResponse response = menuItemService.updateMenuItem(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<?> deleteMenuItem(@PathVariable Long id) {
        try {
            menuItemService.deleteMenuItem(id);
            return ResponseEntity.ok(new MessageResponse("Menu item deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<MenuItemResponse>> getMenuItemsByRestaurant(@PathVariable Long restaurantId) {
        List<MenuItemResponse> items = menuItemService.getMenuItemsByRestaurant(restaurantId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/restaurant/{restaurantId}/available")
    public ResponseEntity<List<MenuItemResponse>> getAvailableMenuItems(@PathVariable Long restaurantId) {
        List<MenuItemResponse> items = menuItemService.getAvailableMenuItems(restaurantId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/restaurant/{restaurantId}/category/{category}")
    public ResponseEntity<List<MenuItemResponse>> getMenuItemsByCategory(
            @PathVariable Long restaurantId,
            @PathVariable Category category) {
        List<MenuItemResponse> items = menuItemService.getMenuItemsByCategory(restaurantId, category);
        return ResponseEntity.ok(items);
    }

    @PatchMapping("/items/{id}/availability")
    public ResponseEntity<?> toggleAvailability(@PathVariable Long id) {
        try {
            MenuItemResponse response = menuItemService.toggleAvailability(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
