// user-service/src/main/java/com/fooddelivery/userservice/service/FavoriteService.java

package com.fooddelivery.userservice.service;

import com.fooddelivery.userservice.entity.Favorite;
import com.fooddelivery.userservice.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    public List<Favorite> getUserFavorites(Long userId) {
        return favoriteRepository.findByUserId(userId);
    }

    public boolean isFavorite(Long userId, Long restaurantId) {
        return favoriteRepository.existsByUserIdAndRestaurantId(userId, restaurantId);
    }

    @Transactional
    public Favorite addFavorite(Long userId, Long restaurantId) {
        // Check if already exists
        if (favoriteRepository.existsByUserIdAndRestaurantId(userId, restaurantId)) {
            throw new IllegalArgumentException("Restaurant already in favorites");
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setRestaurantId(restaurantId);

        return favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(Long userId, Long restaurantId) {
        favoriteRepository.deleteByUserIdAndRestaurantId(userId, restaurantId);
    }

    @Transactional
    public boolean toggleFavorite(Long userId, Long restaurantId) {
        if (favoriteRepository.existsByUserIdAndRestaurantId(userId, restaurantId)) {
            favoriteRepository.deleteByUserIdAndRestaurantId(userId, restaurantId);
            return false; // Removed from favorites
        } else {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setRestaurantId(restaurantId);
            favoriteRepository.save(favorite);
            return true; // Added to favorites
        }
    }
}
