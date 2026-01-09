package com.fooddelivery.promoservice.service;

import com.fooddelivery.promoservice.dto.*;
import com.fooddelivery.promoservice.entity.Coupon;
import com.fooddelivery.promoservice.entity.CouponUsage;
import com.fooddelivery.promoservice.enums.CouponStatus;
import com.fooddelivery.promoservice.enums.DiscountType;
import com.fooddelivery.promoservice.repository.CouponRepository;
import com.fooddelivery.promoservice.repository.CouponUsageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromoService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    // Create Coupon
    @Transactional
    public CouponResponse createCoupon(CreateCouponRequest request) {
        // Check if code already exists
        if (couponRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Coupon code already exists");
        }

        // Validate dates
        if (request.getValidUntil().isBefore(request.getValidFrom())) {
            throw new RuntimeException("Valid until date must be after valid from date");
        }

        Coupon coupon = new Coupon();
        coupon.setCode(request.getCode().toUpperCase());
        coupon.setDescription(request.getDescription());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        coupon.setMinOrderAmount(request.getMinOrderAmount());
        coupon.setValidFrom(request.getValidFrom());
        coupon.setValidUntil(request.getValidUntil());
        coupon.setMaxUsageCount(request.getMaxUsageCount());
        coupon.setMaxUsagePerUser(request.getMaxUsagePerUser());
        coupon.setRestaurantId(request.getRestaurantId());
        coupon.setApplicableFor(request.getApplicableFor() != null ? request.getApplicableFor() : "ALL");
        coupon.setStatus(CouponStatus.ACTIVE);

        coupon = couponRepository.save(coupon);
        return CouponResponse.fromEntity(coupon);
    }

    // Get All Coupons
    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(CouponResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // Get Active Coupons
    public List<CouponResponse> getActiveCoupons() {
        return couponRepository.findByStatus(CouponStatus.ACTIVE).stream()
                .filter(Coupon::isValid)
                .map(CouponResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // Get Coupon by Code
    public CouponResponse getCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        return CouponResponse.fromEntity(coupon);
    }

    // Validate Coupon
    public ValidateCouponResponse validateCoupon(ValidateCouponRequest request) {
        ValidateCouponResponse response = new ValidateCouponResponse();
        response.setCouponCode(request.getCouponCode());

        // Find coupon
        Coupon coupon = couponRepository.findByCode(request.getCouponCode().toUpperCase())
                .orElse(null);

        if (coupon == null) {
            response.setValid(false);
            response.setMessage("Invalid coupon code");
            return response;
        }

        response.setCouponId(coupon.getId());

        // Check if coupon is valid
        if (!coupon.isValid()) {
            response.setValid(false);
            response.setMessage("Coupon is not valid or has expired");
            return response;
        }

        // Check minimum order amount
        if (request.getOrderAmount().compareTo(coupon.getMinOrderAmount()) < 0) {
            response.setValid(false);
            response.setMessage("Minimum order amount is ₹" + coupon.getMinOrderAmount());
            return response;
        }

        // Check restaurant restriction
        if (coupon.getRestaurantId() != null && request.getRestaurantId() != null) {
            if (!coupon.getRestaurantId().equals(request.getRestaurantId())) {
                response.setValid(false);
                response.setMessage("Coupon not applicable for this restaurant");
                return response;
            }
        }

        // Check user usage limit
        int userUsageCount = couponUsageRepository.countByCouponIdAndUserId(coupon.getId(), request.getUserId());
        if (userUsageCount >= coupon.getMaxUsagePerUser()) {
            response.setValid(false);
            response.setMessage("You have already used this coupon maximum times");
            return response;
        }

        // Calculate discount
        BigDecimal discountAmount = calculateDiscount(coupon, request.getOrderAmount());
        BigDecimal finalAmount = request.getOrderAmount().subtract(discountAmount);

        response.setValid(true);
        response.setMessage("Coupon applied successfully");
        response.setDiscountAmount(discountAmount);
        response.setFinalAmount(finalAmount);

        return response;
    }

    // Apply Coupon (Record Usage)
    @Transactional
    public ValidateCouponResponse applyCoupon(ValidateCouponRequest request, Long orderId) {
        // Validate first
        ValidateCouponResponse validation = validateCoupon(request);

        if (!validation.isValid()) {
            return validation;
        }

        // Check if already applied to this order
        if (couponUsageRepository.existsByOrderId(orderId)) {
            validation.setValid(false);
            validation.setMessage("Coupon already applied to this order");
            return validation;
        }

        Coupon coupon = couponRepository.findById(validation.getCouponId())
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        // Record usage
        CouponUsage usage = new CouponUsage();
        usage.setCouponId(coupon.getId());
        usage.setCouponCode(coupon.getCode());
        usage.setUserId(request.getUserId());
        usage.setOrderId(orderId);
        usage.setOrderAmount(request.getOrderAmount());
        usage.setDiscountAmount(validation.getDiscountAmount());
        couponUsageRepository.save(usage);

        // Update coupon usage count
        coupon.setCurrentUsageCount(coupon.getCurrentUsageCount() + 1);
        couponRepository.save(coupon);

        validation.setMessage("Coupon applied successfully to order");
        return validation;
    }

    // Deactivate Coupon
    @Transactional
    public CouponResponse deactivateCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        coupon.setStatus(CouponStatus.INACTIVE);
        coupon = couponRepository.save(coupon);

        return CouponResponse.fromEntity(coupon);
    }

    // Get User Coupon Usage
    public List<CouponUsage> getUserCouponUsage(Long userId) {
        return couponUsageRepository.findByUserId(userId);
    }

    // Helper: Calculate Discount
    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {
        BigDecimal discount = BigDecimal.ZERO;

        switch (coupon.getDiscountType()) {
            case PERCENTAGE:
                discount = orderAmount.multiply(coupon.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                // Apply max discount cap
                if (coupon.getMaxDiscountAmount() != null
                        && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                    discount = coupon.getMaxDiscountAmount();
                }
                break;

            case FLAT_DISCOUNT:
                discount = coupon.getDiscountValue();
                break;

            case FREE_DELIVERY:
                // Delivery charge discount (to be handled by order service)
                discount = BigDecimal.ZERO;
                break;
        }

        return discount;
    }
}
