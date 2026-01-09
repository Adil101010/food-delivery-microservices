package com.fooddelivery.reviewservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_ratings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long deliveryPartnerId;

    @Column(nullable = false)
    private Integer rating; // 1-5 stars

    @Column(length = 500)
    private String comment;

    private Double tipAmount; // Optional tip

    // Detailed ratings
    private Integer punctualityRating; // 1-5

    private Integer behaviourRating; // 1-5

    private Integer packagingRating; // 1-5

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
