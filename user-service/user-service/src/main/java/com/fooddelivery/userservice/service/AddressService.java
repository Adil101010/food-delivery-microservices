// user-service/src/main/java/com/fooddelivery/userservice/service/AddressService.java

package com.fooddelivery.userservice.service;

import com.fooddelivery.userservice.dto.AddressRequest;
import com.fooddelivery.userservice.dto.AddressResponse;
import com.fooddelivery.userservice.entity.Address;
import com.fooddelivery.userservice.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    public List<AddressResponse> getUserAddresses(Long userId) {
        System.out.println("Fetching addresses for user: " + userId);

        List<Address> addresses = addressRepository
                .findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);

        System.out.println("Found " + addresses.size() + " addresses");

        return addresses.stream()
                .map(AddressResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressResponse addAddress(Long userId, AddressRequest request) {
        System.out.println("Adding new address for user: " + userId);

        if (request.getIsDefault() != null && request.getIsDefault()) {
            addressRepository.resetDefaultAddress(userId);
        } else if (addressRepository.countByUserId(userId) == 0) {
            request.setIsDefault(true);
        }

        Address address = new Address();
        address.setUserId(userId);
        address.setLabel(request.getLabel());
        address.setFullAddress(request.getFullAddress());
        address.setLandmark(request.getLandmark());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);

        Address savedAddress = addressRepository.save(address);

        System.out.println("Address saved with ID: " + savedAddress.getId());

        return AddressResponse.fromEntity(savedAddress);
    }

    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request) {
        System.out.println("Updating address ID: " + addressId + " for user: " + userId);

        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (request.getIsDefault() != null && request.getIsDefault() && !address.getIsDefault()) {
            addressRepository.resetDefaultAddress(userId);
        }

        address.setLabel(request.getLabel());
        address.setFullAddress(request.getFullAddress());
        address.setLandmark(request.getLandmark());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());

        if (request.getIsDefault() != null) {
            address.setIsDefault(request.getIsDefault());
        }

        Address updatedAddress = addressRepository.save(address);

        System.out.println("Address updated successfully");

        return AddressResponse.fromEntity(updatedAddress);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        System.out.println("Deleting address ID: " + addressId + " for user: " + userId);

        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        boolean wasDefault = address.getIsDefault();

        addressRepository.deleteByIdAndUserId(addressId, userId);

        if (wasDefault) {
            List<Address> remainingAddresses = addressRepository
                    .findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);

            if (!remainingAddresses.isEmpty()) {
                Address firstAddress = remainingAddresses.get(0);
                firstAddress.setIsDefault(true);
                addressRepository.save(firstAddress);
            }
        }

        System.out.println("Address deleted successfully");
    }

    @Transactional
    public AddressResponse setDefaultAddress(Long userId, Long addressId) {
        System.out.println("Setting default address ID: " + addressId + " for user: " + userId);

        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        addressRepository.resetDefaultAddress(userId);

        address.setIsDefault(true);
        Address updatedAddress = addressRepository.save(address);

        System.out.println("Default address updated successfully");

        return AddressResponse.fromEntity(updatedAddress);
    }

    public AddressResponse getDefaultAddress(Long userId) {
        System.out.println("Fetching default address for user: " + userId);

        return addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .map(AddressResponse::fromEntity)
                .orElseThrow(() -> new RuntimeException("No default address found"));
    }
}
