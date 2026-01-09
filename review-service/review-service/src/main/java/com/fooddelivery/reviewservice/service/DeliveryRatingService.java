package com.fooddelivery.reviewservice.service;

import com.fooddelivery.reviewservice.dto.CreateDeliveryRatingRequest;
import com.fooddelivery.reviewservice.dto.DeliveryRatingResponse;
import com.fooddelivery.reviewservice.dto.RatingStatsResponse;
import com.fooddelivery.reviewservice.entity.DeliveryRating;
import com.fooddelivery.reviewservice.repository.DeliveryRatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryRatingService {

    private final DeliveryRatingRepository deliveryRatingRepository;

    // Create Delivery Rating
    @Transactional
    public DeliveryRatingResponse createDeliveryRating(CreateDeliveryRatingRequest request) {

        // Check if rating already exists for order
        if (deliveryRatingRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new RuntimeException("Delivery rating already exists for this order");
        }

        DeliveryRating rating = new DeliveryRating();
        rating.setOrderId(request.getOrderId());
        rating.setUserId(request.getUserId());
        rating.setDeliveryPartnerId(request.getDeliveryPartnerId());
        rating.setRating(request.getRating());
        rating.setComment(request.getComment());
        rating.setTipAmount(request.getTipAmount());
        rating.setPunctualityRating(request.getPunctualityRating());
        rating.setBehaviourRating(request.getBehaviourRating());
        rating.setPackagingRating(request.getPackagingRating());

        DeliveryRating savedRating = deliveryRatingRepository.save(rating);

        log.info("Delivery rating created for order: {} with rating: {}",
                request.getOrderId(), request.getRating());

        return convertToResponse(savedRating);
    }

    // Get Partner Ratings
    public List<DeliveryRatingResponse> getPartnerRatings(Long partnerId) {
        List<DeliveryRating> ratings = deliveryRatingRepository.findByDeliveryPartnerId(partnerId);
        return ratings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get User's Delivery Ratings
    public List<DeliveryRatingResponse> getUserDeliveryRatings(Long userId) {
        List<DeliveryRating> ratings = deliveryRatingRepository.findByUserId(userId);
        return ratings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get Rating by Order ID
    public DeliveryRatingResponse getRatingByOrderId(Long orderId) {
        DeliveryRating rating = deliveryRatingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery rating not found for order"));
        return convertToResponse(rating);
    }

    // Get Partner Rating Stats
    public RatingStatsResponse getPartnerStats(Long partnerId) {
        Double avgRating = deliveryRatingRepository.getAveragePartnerRating(partnerId);
        Long totalRatings = deliveryRatingRepository.getPartnerRatingCount(partnerId);

        if (avgRating == null) avgRating = 0.0;

        String message = String.format("Partner has %.1f average rating from %d deliveries",
                avgRating, totalRatings);

        return new RatingStatsResponse(partnerId, "PARTNER",
                Math.round(avgRating * 10.0) / 10.0,
                totalRatings, message);
    }

    // Convert to Response
    private DeliveryRatingResponse convertToResponse(DeliveryRating rating) {
        DeliveryRatingResponse response = new DeliveryRatingResponse();
        response.setId(rating.getId());
        response.setOrderId(rating.getOrderId());
        response.setUserId(rating.getUserId());
        response.setDeliveryPartnerId(rating.getDeliveryPartnerId());
        response.setRating(rating.getRating());
        response.setComment(rating.getComment());
        response.setTipAmount(rating.getTipAmount());
        response.setPunctualityRating(rating.getPunctualityRating());
        response.setBehaviourRating(rating.getBehaviourRating());
        response.setPackagingRating(rating.getPackagingRating());
        response.setCreatedAt(rating.getCreatedAt());
        return response;
    }
}
