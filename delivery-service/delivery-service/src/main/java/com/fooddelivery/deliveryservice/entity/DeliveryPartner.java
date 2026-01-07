package com.fooddelivery.deliveryservice.entity;

import com.fooddelivery.deliveryservice.enums.AvailabilityStatus;
import com.fooddelivery.deliveryservice.enums.PartnerStatus;
import com.fooddelivery.deliveryservice.enums.VehicleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_partners")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // Link to User Service

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartnerStatus status = PartnerStatus.PENDING_APPROVAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityStatus availability = AvailabilityStatus.OFFLINE;

    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    private String vehicleNumber;

    private String drivingLicense;

    private String city;

    private String currentLocation; // For now, simple string (Later: Lat/Long)

    @Column(nullable = false)
    private Double rating = 5.0;

    @Column(nullable = false)
    private Integer totalDeliveries = 0;

    @Column(nullable = false)
    private Double totalEarnings = 0.0;

    private String profilePhoto;

    private String address;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
