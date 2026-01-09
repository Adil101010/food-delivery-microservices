package com.fooddelivery.cartservice.dto;

import com.fooddelivery.cartservice.entity.CartItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    private Long id;
    private Long menuItemId;
    private String itemName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String specialInstructions;
    private String imageUrl;

    public static CartItemResponse fromEntity(CartItem item) {
        CartItemResponse response = new CartItemResponse();
        response.setId(item.getId());
        response.setMenuItemId(item.getMenuItemId());
        response.setItemName(item.getItemName());
        response.setPrice(item.getPrice());
        response.setQuantity(item.getQuantity());
        response.setTotalPrice(item.getTotalPrice());
        response.setSpecialInstructions(item.getSpecialInstructions());
        response.setImageUrl(item.getImageUrl());
        return response;
    }
}
