// user-service/src/main/java/com/fooddelivery/userservice/dto/AddressRequest.java

package com.fooddelivery.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    @NotBlank(message = "Label is required")
    private String label;

    @NotBlank(message = "Full address is required")
    @Size(max = 500, message = "Address must be less than 500 characters")
    private String fullAddress;

    private String landmark;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "\\d{6}", message = "Pincode must be 6 digits")
    private String pincode;

    private Double latitude;

    private Double longitude;

    private Boolean isDefault;
}
