package com.fooddelivery.menuservice.dto;

import com.fooddelivery.menuservice.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {

    private Long id;
    private Long restaurantId;
    private String name;
    private String description;
    private Double price;
    private Category category;
    private String imageUrl;
    private Boolean isVegetarian;
    private Boolean isVegan;
    private Boolean isAvailable;
    private Double rating;
    private Integer totalOrders;
    private String ingredients;
    private String allergens;
    private Integer preparationTime;
    private Integer calories;
    private Boolean isBestseller;
    private Boolean isSpicy;
    private Integer spiceLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
