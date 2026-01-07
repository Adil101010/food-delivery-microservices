package com.fooddelivery.deliveryservice.dto;

import com.fooddelivery.deliveryservice.enums.VehicleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartnerRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Name is required")
    private String name;

    @NotNull(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Phone is required")
    private String phone;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @NotNull(message = "Vehicle number is required")
    private String vehicleNumber;

    @NotNull(message = "Driving license is required")
    private String drivingLicense;

    @NotNull(message = "City is required")
    private String city;

    private String address;

    private String profilePhoto;
}
