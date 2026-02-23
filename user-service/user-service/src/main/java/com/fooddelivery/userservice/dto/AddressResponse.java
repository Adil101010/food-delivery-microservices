// user-service/src/main/java/com/fooddelivery/userservice/dto/AddressResponse.java

package com.fooddelivery.userservice.dto;

import com.fooddelivery.userservice.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private Long id;
    private Long userId;
    private String label;
    private String fullAddress;
    private String landmark;
    private String city;
    private String state;
    private String pincode;
    private Double latitude;
    private Double longitude;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AddressResponse fromEntity(Address address) {
        AddressResponse response = new AddressResponse();
        response.setId(address.getId());
        response.setUserId(address.getUserId());
        response.setLabel(address.getLabel());
        response.setFullAddress(address.getFullAddress());
        response.setLandmark(address.getLandmark());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setPincode(address.getPincode());
        response.setLatitude(address.getLatitude());
        response.setLongitude(address.getLongitude());
        response.setIsDefault(address.getIsDefault());
        response.setCreatedAt(address.getCreatedAt());
        response.setUpdatedAt(address.getUpdatedAt());
        return response;
    }
}
