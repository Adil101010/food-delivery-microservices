package com.fooddelivery.promoservice.dto;

import com.fooddelivery.promoservice.entity.Coupon;
import com.fooddelivery.promoservice.enums.CouponStatus;
import com.fooddelivery.promoservice.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponResponse {

    private Long id;
    private String code;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderAmount;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Integer maxUsageCount;
    private Integer maxUsagePerUser;
    private Integer currentUsageCount;
    private Integer remainingUsage;
    private CouponStatus status;
    private Long restaurantId;
    private String applicableFor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CouponResponse fromEntity(Coupon coupon) {
        CouponResponse response = new CouponResponse();
        response.setId(coupon.getId());
        response.setCode(coupon.getCode());
        response.setDescription(coupon.getDescription());
        response.setDiscountType(coupon.getDiscountType());
        response.setDiscountValue(coupon.getDiscountValue());
        response.setMaxDiscountAmount(coupon.getMaxDiscountAmount());
        response.setMinOrderAmount(coupon.getMinOrderAmount());
        response.setValidFrom(coupon.getValidFrom());
        response.setValidUntil(coupon.getValidUntil());
        response.setMaxUsageCount(coupon.getMaxUsageCount());
        response.setMaxUsagePerUser(coupon.getMaxUsagePerUser());
        response.setCurrentUsageCount(coupon.getCurrentUsageCount());
        response.setRemainingUsage(coupon.getMaxUsageCount() - coupon.getCurrentUsageCount());
        response.setStatus(coupon.getStatus());
        response.setRestaurantId(coupon.getRestaurantId());
        response.setApplicableFor(coupon.getApplicableFor());
        response.setCreatedAt(coupon.getCreatedAt());
        response.setUpdatedAt(coupon.getUpdatedAt());
        return response;
    }
}
