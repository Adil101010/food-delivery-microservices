package com.fooddelivery.adminservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderListDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long restaurantId;
    private String restaurantName;
    private String status;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime createdAt;
}
