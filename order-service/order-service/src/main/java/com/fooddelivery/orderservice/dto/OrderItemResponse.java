package com.fooddelivery.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private Long id;
    private Long menuItemId;
    private String itemName;
    private Integer quantity;
    private Double price;
    private Double subtotal;
    private String specialInstructions;
}
