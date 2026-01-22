package com.fooddelivery.searchservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantSearchDTO {
    private Long id;
    private String name;
    private String cuisine;
    private String address;
    private Double rating;
    private Integer deliveryTime; // in minutes
    private Boolean isActive;
    private Boolean isOpen;
    private String phone;
    private String email;
}
