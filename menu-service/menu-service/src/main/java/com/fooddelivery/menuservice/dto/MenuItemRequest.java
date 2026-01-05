package com.fooddelivery.menuservice.dto;

import com.fooddelivery.menuservice.entity.Category;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MenuItemRequest {

    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;

    @NotBlank(message = "Item name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be positive")
    private Double price;

    @NotNull(message = "Category is required")
    private Category category;

    private String imageUrl;

    private Boolean isVegetarian;

    private Boolean isVegan;

    private Boolean isAvailable;

    private String ingredients;

    private String allergens;

    private Integer preparationTime;

    private Integer calories;

    private Boolean isBestseller;

    private Boolean isSpicy;

    private Integer spiceLevel;
}
