package com.fooddelivery.reviewservice.controller;

import com.fooddelivery.reviewservice.dto.CreateDeliveryRatingRequest;
import com.fooddelivery.reviewservice.dto.DeliveryRatingResponse;
import com.fooddelivery.reviewservice.dto.RatingStatsResponse;
import com.fooddelivery.reviewservice.service.DeliveryRatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery-ratings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DeliveryRatingController {

    private final DeliveryRatingService deliveryRatingService;

    // Create Delivery Rating
    @PostMapping
    public ResponseEntity<DeliveryRatingResponse> createDeliveryRating(
            @Valid @RequestBody CreateDeliveryRatingRequest request) {
        DeliveryRatingResponse rating = deliveryRatingService.createDeliveryRating(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(rating);
    }

    // Get Partner Ratings
    @GetMapping("/partner/{partnerId}")
    public ResponseEntity<List<DeliveryRatingResponse>> getPartnerRatings(@PathVariable Long partnerId) {
        List<DeliveryRatingResponse> ratings = deliveryRatingService.getPartnerRatings(partnerId);
        return ResponseEntity.ok(ratings);
    }

    // Get User's Delivery Ratings
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DeliveryRatingResponse>> getUserDeliveryRatings(@PathVariable Long userId) {
        List<DeliveryRatingResponse> ratings = deliveryRatingService.getUserDeliveryRatings(userId);
        return ResponseEntity.ok(ratings);
    }

    // Get Rating by Order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryRatingResponse> getRatingByOrderId(@PathVariable Long orderId) {
        DeliveryRatingResponse rating = deliveryRatingService.getRatingByOrderId(orderId);
        return ResponseEntity.ok(rating);
    }

    // Get Partner Stats
    @GetMapping("/partner/{partnerId}/stats")
    public ResponseEntity<RatingStatsResponse> getPartnerStats(@PathVariable Long partnerId) {
        RatingStatsResponse stats = deliveryRatingService.getPartnerStats(partnerId);
        return ResponseEntity.ok(stats);
    }
}
