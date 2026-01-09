package com.fooddelivery.reviewservice.controller;

import com.fooddelivery.reviewservice.dto.*;
import com.fooddelivery.reviewservice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewService reviewService;

    // Health Check
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        return ResponseEntity.ok(new MessageResponse("Review Service is running"));
    }

    // Create Review
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse review = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    // Get Restaurant Reviews
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<ReviewResponse>> getRestaurantReviews(@PathVariable Long restaurantId) {
        List<ReviewResponse> reviews = reviewService.getRestaurantReviews(restaurantId);
        return ResponseEntity.ok(reviews);
    }

    // Get User Reviews
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getUserReviews(@PathVariable Long userId) {
        List<ReviewResponse> reviews = reviewService.getUserReviews(userId);
        return ResponseEntity.ok(reviews);
    }

    // Get Review by Order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ReviewResponse> getReviewByOrderId(@PathVariable Long orderId) {
        ReviewResponse review = reviewService.getReviewByOrderId(orderId);
        return ResponseEntity.ok(review);
    }

    // Get Menu Item Reviews
    @GetMapping("/menu-item/{menuItemId}")
    public ResponseEntity<List<ReviewResponse>> getMenuItemReviews(@PathVariable Long menuItemId) {
        List<ReviewResponse> reviews = reviewService.getMenuItemReviews(menuItemId);
        return ResponseEntity.ok(reviews);
    }

    // Get Restaurant Stats
    @GetMapping("/restaurant/{restaurantId}/stats")
    public ResponseEntity<RatingStatsResponse> getRestaurantStats(@PathVariable Long restaurantId) {
        RatingStatsResponse stats = reviewService.getRestaurantStats(restaurantId);
        return ResponseEntity.ok(stats);
    }

    // Mark Review as Helpful
    @PostMapping("/{reviewId}/helpful")
    public ResponseEntity<ReviewResponse> markHelpful(@PathVariable Long reviewId) {
        ReviewResponse review = reviewService.markHelpful(reviewId);
        return ResponseEntity.ok(review);
    }

    // Report Review
    @PostMapping("/{reviewId}/report")
    public ResponseEntity<ReviewResponse> reportReview(@PathVariable Long reviewId) {
        ReviewResponse review = reviewService.reportReview(reviewId);
        return ResponseEntity.ok(review);
    }

    // Add Restaurant Reply
    @PostMapping("/{reviewId}/reply")
    public ResponseEntity<ReviewResponse> addRestaurantReply(
            @PathVariable Long reviewId,
            @RequestParam String reply) {
        ReviewResponse review = reviewService.addRestaurantReply(reviewId, reply);
        return ResponseEntity.ok(review);
    }
}
