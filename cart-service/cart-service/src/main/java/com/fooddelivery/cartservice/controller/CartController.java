package com.fooddelivery.cartservice.controller;

import com.fooddelivery.cartservice.dto.*;
import com.fooddelivery.cartservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    // Health Check
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        return ResponseEntity.ok(new MessageResponse("Cart Service is running"));
    }

    // Get Cart by User ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable Long userId) {
        CartResponse cart = cartService.getCart(userId);
        return ResponseEntity.ok(cart);
    }

    // Add Item to Cart
    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(@Valid @RequestBody AddToCartRequest request) {
        CartResponse cart = cartService.addToCart(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }

    // Update Cart Item
    @PutMapping("/user/{userId}/item/{itemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartResponse cart = cartService.updateCartItem(userId, itemId, request);
        return ResponseEntity.ok(cart);
    }

    // Remove Item from Cart
    @DeleteMapping("/user/{userId}/item/{itemId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @PathVariable Long userId,
            @PathVariable Long itemId) {
        CartResponse cart = cartService.removeFromCart(userId, itemId);
        return ResponseEntity.ok(cart);
    }

    // Clear Cart
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<MessageResponse> clearCart(@PathVariable Long userId) {
        MessageResponse response = cartService.clearCart(userId);
        return ResponseEntity.ok(response);
    }

    // Apply Coupon
    @PostMapping("/user/{userId}/coupon")
    public ResponseEntity<CartResponse> applyCoupon(
            @PathVariable Long userId,
            @RequestParam String couponCode,
            @RequestParam BigDecimal discountAmount) {
        CartResponse cart = cartService.applyCoupon(userId, couponCode, discountAmount);
        return ResponseEntity.ok(cart);
    }

    // Remove Coupon
    @DeleteMapping("/user/{userId}/coupon")
    public ResponseEntity<CartResponse> removeCoupon(@PathVariable Long userId) {
        CartResponse cart = cartService.removeCoupon(userId);
        return ResponseEntity.ok(cart);
    }
}
