package com.fooddelivery.reviewservice.service;

import com.fooddelivery.reviewservice.dto.*;
import com.fooddelivery.reviewservice.entity.Review;
import com.fooddelivery.reviewservice.enums.ReviewStatus;
import com.fooddelivery.reviewservice.enums.ReviewType;
import com.fooddelivery.reviewservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;

    // Create Review
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {

        // Check if review already exists for order
        if (reviewRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new RuntimeException("Review already exists for this order");
        }

        Review review = new Review();
        review.setUserId(request.getUserId());
        review.setOrderId(request.getOrderId());
        review.setType(request.getType());
        review.setRestaurantId(request.getRestaurantId());
        review.setMenuItemId(request.getMenuItemId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setPhotos(request.getPhotos());
        review.setStatus(ReviewStatus.APPROVED);
        review.setIsVerifiedPurchase(true); // Since linked to order
        review.setHelpfulCount(0);
        review.setReportCount(0);

        Review savedReview = reviewRepository.save(review);

        log.info("Review created for order: {} with rating: {}", request.getOrderId(), request.getRating());

        return convertToResponse(savedReview);
    }

    // Get Restaurant Reviews
    public List<ReviewResponse> getRestaurantReviews(Long restaurantId) {
        List<Review> reviews = reviewRepository.findByRestaurantId(restaurantId);
        return reviews.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get User Reviews
    public List<ReviewResponse> getUserReviews(Long userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);
        return reviews.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get Review by Order ID
    public ReviewResponse getReviewByOrderId(Long orderId) {
        Review review = reviewRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Review not found for order"));
        return convertToResponse(review);
    }

    // Get Menu Item Reviews
    public List<ReviewResponse> getMenuItemReviews(Long menuItemId) {
        List<Review> reviews = reviewRepository.findByMenuItemId(menuItemId);
        return reviews.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get Restaurant Rating Stats
    public RatingStatsResponse getRestaurantStats(Long restaurantId) {
        Double avgRating = reviewRepository.getAverageRating(restaurantId, ReviewType.RESTAURANT);
        Long totalReviews = reviewRepository.getReviewCount(restaurantId);

        if (avgRating == null) avgRating = 0.0;

        String message = String.format("Restaurant has %.1f average rating from %d reviews",
                avgRating, totalReviews);

        return new RatingStatsResponse(restaurantId, "RESTAURANT",
                Math.round(avgRating * 10.0) / 10.0,
                totalReviews, message);
    }

    // Mark Review as Helpful
    @Transactional
    public ReviewResponse markHelpful(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        review.setHelpfulCount(review.getHelpfulCount() + 1);
        Review updated = reviewRepository.save(review);

        return convertToResponse(updated);
    }

    // Report Review
    @Transactional
    public ReviewResponse reportReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        review.setReportCount(review.getReportCount() + 1);

        // Auto-flag if reported 3+ times
        if (review.getReportCount() >= 3) {
            review.setStatus(ReviewStatus.FLAGGED);
        }

        Review updated = reviewRepository.save(review);
        return convertToResponse(updated);
    }

    // Restaurant Reply
    @Transactional
    public ReviewResponse addRestaurantReply(Long reviewId, String reply) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        review.setRestaurantReply(reply);
        review.setRepliedAt(LocalDateTime.now());

        Review updated = reviewRepository.save(review);
        return convertToResponse(updated);
    }

    // Convert to Response
    private ReviewResponse convertToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setUserId(review.getUserId());
        response.setOrderId(review.getOrderId());
        response.setType(review.getType());
        response.setRestaurantId(review.getRestaurantId());
        response.setMenuItemId(review.getMenuItemId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setPhotos(review.getPhotos());
        response.setStatus(review.getStatus());
        response.setIsVerifiedPurchase(review.getIsVerifiedPurchase());
        response.setHelpfulCount(review.getHelpfulCount());
        response.setReportCount(review.getReportCount());
        response.setRestaurantReply(review.getRestaurantReply());
        response.setRepliedAt(review.getRepliedAt());
        response.setCreatedAt(review.getCreatedAt());
        response.setUpdatedAt(review.getUpdatedAt());
        return response;
    }
}
