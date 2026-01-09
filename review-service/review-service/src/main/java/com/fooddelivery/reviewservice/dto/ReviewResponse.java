package com.fooddelivery.reviewservice.dto;

import com.fooddelivery.reviewservice.enums.ReviewStatus;
import com.fooddelivery.reviewservice.enums.ReviewType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private Long userId;
    private Long orderId;
    private ReviewType type;
    private Long restaurantId;
    private Long menuItemId;
    private Integer rating;
    private String comment;
    private String photos;
    private ReviewStatus status;
    private Boolean isVerifiedPurchase;
    private Integer helpfulCount;
    private Integer reportCount;
    private String restaurantReply;
    private LocalDateTime repliedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
