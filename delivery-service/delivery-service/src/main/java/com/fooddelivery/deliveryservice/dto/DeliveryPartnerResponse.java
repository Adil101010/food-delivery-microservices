package com.fooddelivery.deliveryservice.dto;

import com.fooddelivery.deliveryservice.enums.AvailabilityStatus;
import com.fooddelivery.deliveryservice.enums.PartnerStatus;
import com.fooddelivery.deliveryservice.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartnerResponse {

    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private PartnerStatus status;
    private AvailabilityStatus availability;
    private VehicleType vehicleType;
    private String vehicleNumber;
    private String drivingLicense;
    private String city;
    private String currentLocation;
    private Double rating;
    private Integer totalDeliveries;
    private Double totalEarnings;
    private String profilePhoto;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
