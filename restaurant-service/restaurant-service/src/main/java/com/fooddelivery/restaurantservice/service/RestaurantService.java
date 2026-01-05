package com.fooddelivery.restaurantservice.service;

import com.fooddelivery.restaurantservice.dto.RestaurantRequest;
import com.fooddelivery.restaurantservice.dto.RestaurantResponse;
import com.fooddelivery.restaurantservice.entity.Restaurant;
import com.fooddelivery.restaurantservice.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    public RestaurantResponse registerRestaurant(RestaurantRequest request) {
        Restaurant restaurant = new Restaurant();
        restaurant.setOwnerId(request.getOwnerId());
        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(request.getCity());
        restaurant.setState(request.getState());
        restaurant.setPincode(request.getPincode());
        restaurant.setPhone(request.getPhone());
        restaurant.setEmail(request.getEmail());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setOpeningTime(request.getOpeningTime());
        restaurant.setClosingTime(request.getClosingTime());
        restaurant.setImageUrl(request.getImageUrl());
        restaurant.setDeliveryFee(request.getDeliveryFee() != null ? request.getDeliveryFee() : 0.0);
        restaurant.setMinOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : 0);
        restaurant.setAvgDeliveryTime(request.getAvgDeliveryTime() != null ? request.getAvgDeliveryTime() : 30);

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return mapToResponse(savedRestaurant);
    }

    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
        return mapToResponse(restaurant);
    }

    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));

        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(request.getCity());
        restaurant.setState(request.getState());
        restaurant.setPincode(request.getPincode());
        restaurant.setPhone(request.getPhone());
        restaurant.setEmail(request.getEmail());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setOpeningTime(request.getOpeningTime());
        restaurant.setClosingTime(request.getClosingTime());
        restaurant.setImageUrl(request.getImageUrl());
        restaurant.setDeliveryFee(request.getDeliveryFee());
        restaurant.setMinOrderAmount(request.getMinOrderAmount());
        restaurant.setAvgDeliveryTime(request.getAvgDeliveryTime());

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return mapToResponse(updatedRestaurant);
    }

    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<RestaurantResponse> getRestaurantsByCity(String city) {
        return restaurantRepository.findByCityAndIsActiveTrue(city).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<RestaurantResponse> searchRestaurantsByCuisine(String cuisine) {
        return restaurantRepository.findByCuisineContaining(cuisine).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private RestaurantResponse mapToResponse(Restaurant restaurant) {
        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getOwnerId(),
                restaurant.getName(),
                restaurant.getDescription(),
                restaurant.getAddress(),
                restaurant.getCity(),
                restaurant.getState(),
                restaurant.getPincode(),
                restaurant.getPhone(),
                restaurant.getEmail(),
                restaurant.getCuisine(),
                restaurant.getOpeningTime(),
                restaurant.getClosingTime(),
                restaurant.getRating(),
                restaurant.getTotalReviews(),
                restaurant.getImageUrl(),
                restaurant.getIsActive(),
                restaurant.getIsOpen(),
                restaurant.getDeliveryFee(),
                restaurant.getMinOrderAmount(),
                restaurant.getAvgDeliveryTime(),
                restaurant.getCreatedAt(),
                restaurant.getUpdatedAt()
        );
    }
}
