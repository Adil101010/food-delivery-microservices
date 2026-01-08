package com.fooddelivery.locationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "partner_locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long partnerId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private Double speed; // km/h

    private Double heading; // Direction in degrees (0-360)

    private Double accuracy; // meters

    @Column(nullable = false)
    private Boolean isMoving = false;

    @Column(nullable = false)
    private Boolean isOnline = false;

    private Long currentDeliveryId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
