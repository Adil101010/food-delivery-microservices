package com.fooddelivery.searchservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchFilters {
    private String query;
    private String cuisine;
    private Double minRating;
    private Integer maxDeliveryTime;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String category; // VEG, NON_VEG, VEGAN
    private Boolean isActive;
    private String sortBy; // rating, deliveryTime, name
}
