package com.fooddelivery.promoservice.controller;

import com.fooddelivery.promoservice.dto.*;
import com.fooddelivery.promoservice.entity.CouponUsage;
import com.fooddelivery.promoservice.service.PromoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PromoController {

    private final PromoService promoService;

    // Health Check
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        return ResponseEntity.ok(new MessageResponse("Promo Service is running"));
    }

    // Create Coupon
    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        CouponResponse coupon = promoService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(coupon);
    }

    // Get All Coupons
    @GetMapping
    public ResponseEntity<List<CouponResponse>> getAllCoupons() {
        List<CouponResponse> coupons = promoService.getAllCoupons();
        return ResponseEntity.ok(coupons);
    }

    // Get Active Coupons
    @GetMapping("/active")
    public ResponseEntity<List<CouponResponse>> getActiveCoupons() {
        List<CouponResponse> coupons = promoService.getActiveCoupons();
        return ResponseEntity.ok(coupons);
    }

    // Get Coupon by Code
    @GetMapping("/code/{code}")
    public ResponseEntity<CouponResponse> getCouponByCode(@PathVariable String code) {
        CouponResponse coupon = promoService.getCouponByCode(code);
        return ResponseEntity.ok(coupon);
    }

    // Validate Coupon
    @PostMapping("/validate")
    public ResponseEntity<ValidateCouponResponse> validateCoupon(
            @Valid @RequestBody ValidateCouponRequest request) {
        ValidateCouponResponse response = promoService.validateCoupon(request);
        return ResponseEntity.ok(response);
    }

    // Apply Coupon to Order
    @PostMapping("/apply")
    public ResponseEntity<ValidateCouponResponse> applyCoupon(
            @Valid @RequestBody ValidateCouponRequest request,
            @RequestParam Long orderId) {
        ValidateCouponResponse response = promoService.applyCoupon(request, orderId);
        return ResponseEntity.ok(response);
    }

    // Deactivate Coupon
    @PutMapping("/{couponId}/deactivate")
    public ResponseEntity<CouponResponse> deactivateCoupon(@PathVariable Long couponId) {
        CouponResponse coupon = promoService.deactivateCoupon(couponId);
        return ResponseEntity.ok(coupon);
    }

    // Get User Coupon Usage History
    @GetMapping("/user/{userId}/usage")
    public ResponseEntity<List<CouponUsage>> getUserCouponUsage(@PathVariable Long userId) {
        List<CouponUsage> usage = promoService.getUserCouponUsage(userId);
        return ResponseEntity.ok(usage);
    }
}
