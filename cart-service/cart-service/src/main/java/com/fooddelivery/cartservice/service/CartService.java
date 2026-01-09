package com.fooddelivery.cartservice.service;

import com.fooddelivery.cartservice.dto.*;
import com.fooddelivery.cartservice.entity.Cart;
import com.fooddelivery.cartservice.entity.CartItem;
import com.fooddelivery.cartservice.repository.CartItemRepository;
import com.fooddelivery.cartservice.repository.CartRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    // Get or Create Cart for User
    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createEmptyCart(userId));
        return CartResponse.fromEntity(cart);
    }

    // Add Item to Cart
    @Transactional
    public CartResponse addToCart(AddToCartRequest request) {
        Cart cart = cartRepository.findByUserId(request.getUserId())
                .orElse(null);

        // If cart exists but from different restaurant, clear items and update restaurant
        if (cart != null && !cart.getRestaurantId().equals(request.getRestaurantId()) && cart.getRestaurantId() != 0) {
            // Different restaurant - clear old cart items
            cartItemRepository.deleteByCartId(cart.getId());
            cart.getItems().clear();
            cart.setRestaurantId(request.getRestaurantId());
            cart.setRestaurantName(request.getRestaurantName());
            cart.setCouponCode(null);
            cart.setDiscount(BigDecimal.ZERO);
            cart.setSubtotal(BigDecimal.ZERO);
            cart.setTotal(BigDecimal.ZERO);
            cart = cartRepository.save(cart);
        }

        // Create new cart if doesn't exist
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(request.getUserId());
            cart.setRestaurantId(request.getRestaurantId());
            cart.setRestaurantName(request.getRestaurantName());
            cart = cartRepository.save(cart);
        }

        // If cart was empty (restaurantId = 0), update restaurant info
        if (cart.getRestaurantId() == 0) {
            cart.setRestaurantId(request.getRestaurantId());
            cart.setRestaurantName(request.getRestaurantName());
            cart = cartRepository.save(cart);
        }

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndMenuItemId(cart.getId(), request.getMenuItemId());

        CartItem cartItem;
        if (existingItem.isPresent()) {
            // Update quantity if item exists
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            cartItem.calculateTotalPrice();
        } else {
            // Create new cart item
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setMenuItemId(request.getMenuItemId());
            cartItem.setItemName(request.getItemName());
            cartItem.setPrice(request.getPrice());
            cartItem.setQuantity(request.getQuantity());
            cartItem.setSpecialInstructions(request.getSpecialInstructions());
            cartItem.setImageUrl(request.getImageUrl());
            cartItem.calculateTotalPrice();
            cart.addItem(cartItem);
        }

        cartItemRepository.save(cartItem);
        cart.calculateTotal();
        cart = cartRepository.save(cart);

        return CartResponse.fromEntity(cart);
    }

    // Update Cart Item
    @Transactional
    public CartResponse updateCartItem(Long userId, Long itemId, UpdateCartItemRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item does not belong to this cart");
        }

        cartItem.setQuantity(request.getQuantity());
        cartItem.setSpecialInstructions(request.getSpecialInstructions());
        cartItem.calculateTotalPrice();
        cartItemRepository.save(cartItem);

        cart.calculateTotal();
        cart = cartRepository.save(cart);

        return CartResponse.fromEntity(cart);
    }

    // Remove Item from Cart
    @Transactional
    public CartResponse removeFromCart(Long userId, Long itemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item does not belong to this cart");
        }

        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        cart.calculateTotal();
        cart = cartRepository.save(cart);

        return CartResponse.fromEntity(cart);
    }

    // Clear Cart
    @Transactional
    public MessageResponse clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cartItemRepository.deleteByCartId(cart.getId());
        cartRepository.delete(cart);

        return new MessageResponse("Cart cleared successfully");
    }

    // Apply Coupon
    @Transactional
    public CartResponse applyCoupon(Long userId, String couponCode, BigDecimal discountAmount) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.setCouponCode(couponCode);
        cart.setDiscount(discountAmount);
        cart.calculateTotal();
        cart = cartRepository.save(cart);

        return CartResponse.fromEntity(cart);
    }

    // Remove Coupon
    @Transactional
    public CartResponse removeCoupon(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.setCouponCode(null);
        cart.setDiscount(BigDecimal.ZERO);
        cart.calculateTotal();
        cart = cartRepository.save(cart);

        return CartResponse.fromEntity(cart);
    }

    // Helper method to create empty cart
    private Cart createEmptyCart(Long userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setRestaurantId(0L);
        cart.setRestaurantName("Empty");
        return cartRepository.save(cart);
    }
}
