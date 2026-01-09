package com.fooddelivery.promoservice.dto;

import com.fooddelivery.promoservice.enums.DiscountType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCouponRequest {

    @NotBlank(message = "Coupon code is required")
    @Size(min = 3, max = 50, message = "Code must be 3-50 characters")
    private String code;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount must be positive")
    private BigDecimal discountValue;

    private BigDecimal maxDiscountAmount;

    @NotNull(message = "Minimum order amount is required")
    @DecimalMin(value = "0.00", message = "Min order amount must be positive")
    private BigDecimal minOrderAmount;

    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;

    @NotNull(message = "Valid until date is required")
    private LocalDateTime validUntil;

    @NotNull(message = "Max usage count is required")
    @Min(value = 1, message = "Max usage must be at least 1")
    private Integer maxUsageCount;

    @NotNull(message = "Max usage per user is required")
    @Min(value = 1, message = "Max usage per user must be at least 1")
    private Integer maxUsagePerUser;

    private Long restaurantId;

    private String applicableFor; // "ALL", "FIRST_ORDER", "EXISTING_USERS"
}
