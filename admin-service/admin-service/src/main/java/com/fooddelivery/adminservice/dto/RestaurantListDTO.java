package com.fooddelivery.adminservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantListDTO {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String cuisine;
    private String status; // PENDING, APPROVED, REJECTED, BLOCKED
    private Boolean isActive;
    private Double rating;
    private LocalDateTime createdAt;
    private Long totalOrders;
}
