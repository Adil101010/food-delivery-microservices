// user-service/src/main/java/com/fooddelivery/userservice/repository/FavoriteRepository.java

package com.fooddelivery.userservice.repository;

import com.fooddelivery.userservice.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserId(Long userId);

    Optional<Favorite> findByUserIdAndRestaurantId(Long userId, Long restaurantId);

    boolean existsByUserIdAndRestaurantId(Long userId, Long restaurantId);

    void deleteByUserIdAndRestaurantId(Long userId, Long restaurantId);
}
