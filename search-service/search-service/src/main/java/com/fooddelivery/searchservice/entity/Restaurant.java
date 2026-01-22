package com.fooddelivery.searchservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "restaurants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String cuisine;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String phone;

    private String email;

    private Double rating = 0.0;

    @Column(name = "delivery_time")
    private Integer deliveryTime; // in minutes

    @Column(name = "is_active")
    private Boolean isActive = true;

    private String status; // PENDING, APPROVED, REJECTED, BLOCKED

    @Column(name = "opening_time")
    private String openingTime;

    @Column(name = "closing_time")
    private String closingTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
