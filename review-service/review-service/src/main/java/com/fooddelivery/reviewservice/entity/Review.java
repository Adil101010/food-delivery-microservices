package com.fooddelivery.reviewservice.entity;

import com.fooddelivery.reviewservice.enums.ReviewStatus;
import com.fooddelivery.reviewservice.enums.ReviewType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewType type;

    @Column(nullable = false)
    private Long restaurantId;

    private Long menuItemId; // Optional, for menu item reviews

    @Column(nullable = false)
    private Integer rating; // 1-5 stars

    @Column(length = 1000)
    private String comment;

    @Column(length = 500)
    private String photos; // Comma-separated photo URLs

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.APPROVED;

    @Column(nullable = false)
    private Boolean isVerifiedPurchase = false;

    private Integer helpfulCount = 0;

    private Integer reportCount = 0;

    private String restaurantReply;

    private LocalDateTime repliedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
