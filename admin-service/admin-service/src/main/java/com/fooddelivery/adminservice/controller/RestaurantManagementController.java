package com.fooddelivery.adminservice.controller;

import com.fooddelivery.adminservice.dto.ActionRequest;
import com.fooddelivery.adminservice.dto.MessageResponse;
import com.fooddelivery.adminservice.dto.RestaurantListDTO;
import com.fooddelivery.adminservice.service.RestaurantManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/restaurants")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RestaurantManagementController {

    private final RestaurantManagementService restaurantManagementService;

    // Get All Restaurants
    @GetMapping
    public ResponseEntity<List<RestaurantListDTO>> getAllRestaurants() {
        List<RestaurantListDTO> restaurants = restaurantManagementService.getAllRestaurants();
        return ResponseEntity.ok(restaurants);
    }

    // Get Pending Restaurants (For Approval)
    @GetMapping("/pending")
    public ResponseEntity<List<RestaurantListDTO>> getPendingRestaurants() {
        List<RestaurantListDTO> restaurants = restaurantManagementService.getPendingRestaurants();
        return ResponseEntity.ok(restaurants);
    }

    // Get Restaurant by ID
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantListDTO> getRestaurantById(@PathVariable Long id) {
        RestaurantListDTO restaurant = restaurantManagementService.getRestaurantById(id);
        return ResponseEntity.ok(restaurant);
    }

    // Approve Restaurant
    @PutMapping("/approve")
    public ResponseEntity<MessageResponse> approveRestaurant(@Valid @RequestBody ActionRequest request) {
        restaurantManagementService.approveRestaurant(request.getId());
        return ResponseEntity.ok(new MessageResponse("Restaurant approved successfully"));
    }

    // Reject Restaurant
    @PutMapping("/reject")
    public ResponseEntity<MessageResponse> rejectRestaurant(@Valid @RequestBody ActionRequest request) {
        restaurantManagementService.rejectRestaurant(request.getId(), request.getReason());
        return ResponseEntity.ok(new MessageResponse("Restaurant rejected successfully"));
    }

    // Block Restaurant
    @PutMapping("/block")
    public ResponseEntity<MessageResponse> blockRestaurant(@Valid @RequestBody ActionRequest request) {
        restaurantManagementService.blockRestaurant(request.getId());
        return ResponseEntity.ok(new MessageResponse("Restaurant blocked successfully"));
    }

    // Unblock Restaurant
    @PutMapping("/unblock")
    public ResponseEntity<MessageResponse> unblockRestaurant(@Valid @RequestBody ActionRequest request) {
        restaurantManagementService.unblockRestaurant(request.getId());
        return ResponseEntity.ok(new MessageResponse("Restaurant unblocked successfully"));
    }
}
