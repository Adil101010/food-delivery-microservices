// user-service/src/main/java/com/fooddelivery/userservice/controller/FavoriteController.java

package com.fooddelivery.userservice.controller;

import com.fooddelivery.userservice.entity.Favorite;
import com.fooddelivery.userservice.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Favorite>> getUserFavorites(@PathVariable Long userId) {
        List<Favorite> favorites = favoriteService.getUserFavorites(userId);
        return ResponseEntity.ok(favorites);
    }

    @GetMapping("/check/{userId}/{restaurantId}")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(
            @PathVariable Long userId,
            @PathVariable Long restaurantId) {
        boolean isFavorite = favoriteService.isFavorite(userId, restaurantId);
        return ResponseEntity.ok(Map.of("isFavorite", isFavorite));
    }

    @PostMapping
    public ResponseEntity<Favorite> addFavorite(@RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        Long restaurantId = request.get("restaurantId");

        try {
            Favorite favorite = favoriteService.addFavorite(userId, restaurantId);
            return ResponseEntity.status(HttpStatus.CREATED).body(favorite);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/{userId}/{restaurantId}")
    public ResponseEntity<Void> removeFavorite(
            @PathVariable Long userId,
            @PathVariable Long restaurantId) {
        favoriteService.removeFavorite(userId, restaurantId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Boolean>> toggleFavorite(@RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        Long restaurantId = request.get("restaurantId");

        boolean isFavorite = favoriteService.toggleFavorite(userId, restaurantId);
        return ResponseEntity.ok(Map.of("isFavorite", isFavorite));
    }
}
