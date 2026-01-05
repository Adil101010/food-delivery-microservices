package com.fooddelivery.restaurantservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {

    private Long id;
    private Long ownerId;
    private String name;
    private String description;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String phone;
    private String email;
    private String cuisine;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Double rating;
    private Integer totalReviews;
    private String imageUrl;
    private Boolean isActive;
    private Boolean isOpen;
    private Double deliveryFee;
    private Integer minOrderAmount;
    private Integer avgDeliveryTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
