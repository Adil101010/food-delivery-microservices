package com.fooddelivery.deliveryservice.service;

import com.fooddelivery.deliveryservice.dto.DeliveryPartnerRequest;
import com.fooddelivery.deliveryservice.dto.DeliveryPartnerResponse;
import com.fooddelivery.deliveryservice.entity.DeliveryPartner;
import com.fooddelivery.deliveryservice.enums.AvailabilityStatus;
import com.fooddelivery.deliveryservice.enums.PartnerStatus;
import com.fooddelivery.deliveryservice.repository.DeliveryPartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryPartnerService {

    private final DeliveryPartnerRepository partnerRepository;

    // Register Delivery Partner
    @Transactional
    public DeliveryPartnerResponse registerPartner(DeliveryPartnerRequest request) {

        // Check if email exists
        if (partnerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Check if phone exists
        if (partnerRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new RuntimeException("Phone already registered");
        }

        DeliveryPartner partner = new DeliveryPartner();
        partner.setUserId(request.getUserId());
        partner.setName(request.getName());
        partner.setEmail(request.getEmail());
        partner.setPhone(request.getPhone());
        partner.setVehicleType(request.getVehicleType());
        partner.setVehicleNumber(request.getVehicleNumber());
        partner.setDrivingLicense(request.getDrivingLicense());
        partner.setCity(request.getCity());
        partner.setAddress(request.getAddress());
        partner.setProfilePhoto(request.getProfilePhoto());
        partner.setStatus(PartnerStatus.PENDING_APPROVAL);
        partner.setAvailability(AvailabilityStatus.OFFLINE);

        DeliveryPartner savedPartner = partnerRepository.save(partner);
        return convertToResponse(savedPartner);
    }

    // Get Partner by ID
    public DeliveryPartnerResponse getPartnerById(Long id) {
        DeliveryPartner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partner not found"));
        return convertToResponse(partner);
    }

    // Get Partner by User ID
    public DeliveryPartnerResponse getPartnerByUserId(Long userId) {
        DeliveryPartner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));
        return convertToResponse(partner);
    }

    // Update Partner Profile
    @Transactional
    public DeliveryPartnerResponse updatePartner(Long id, DeliveryPartnerRequest request) {
        DeliveryPartner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        partner.setName(request.getName());
        partner.setPhone(request.getPhone());
        partner.setVehicleType(request.getVehicleType());
        partner.setVehicleNumber(request.getVehicleNumber());
        partner.setCity(request.getCity());
        partner.setAddress(request.getAddress());
        partner.setProfilePhoto(request.getProfilePhoto());

        DeliveryPartner updatedPartner = partnerRepository.save(partner);
        return convertToResponse(updatedPartner);
    }

    // Update Partner Status (Admin)
    @Transactional
    public DeliveryPartnerResponse updatePartnerStatus(Long id, PartnerStatus status) {
        DeliveryPartner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        partner.setStatus(status);
        DeliveryPartner updatedPartner = partnerRepository.save(partner);
        return convertToResponse(updatedPartner);
    }

    // Update Availability (Online/Offline)
    @Transactional
    public DeliveryPartnerResponse updateAvailability(Long id, AvailabilityStatus availability) {
        DeliveryPartner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        partner.setAvailability(availability);
        DeliveryPartner updatedPartner = partnerRepository.save(partner);
        return convertToResponse(updatedPartner);
    }

    // Get Available Partners in City
    public List<DeliveryPartnerResponse> getAvailablePartnersInCity(String city) {
        List<DeliveryPartner> partners = partnerRepository
                .findByCityAndAvailability(city, AvailabilityStatus.ONLINE);
        return partners.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get All Partners
    public List<DeliveryPartnerResponse> getAllPartners() {
        List<DeliveryPartner> partners = partnerRepository.findAll();
        return partners.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Update Partner Stats (After Delivery)
    @Transactional
    public void updatePartnerStats(Long partnerId, Double earning, Double rating) {
        DeliveryPartner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        partner.setTotalDeliveries(partner.getTotalDeliveries() + 1);
        partner.setTotalEarnings(partner.getTotalEarnings() + earning);

        // Calculate new average rating
        Double currentRating = partner.getRating();
        Integer totalDeliveries = partner.getTotalDeliveries();
        Double newRating = ((currentRating * (totalDeliveries - 1)) + rating) / totalDeliveries;
        partner.setRating(Math.round(newRating * 10.0) / 10.0);

        partnerRepository.save(partner);
    }

    // Convert Entity to Response
    private DeliveryPartnerResponse convertToResponse(DeliveryPartner partner) {
        DeliveryPartnerResponse response = new DeliveryPartnerResponse();
        response.setId(partner.getId());
        response.setUserId(partner.getUserId());
        response.setName(partner.getName());
        response.setEmail(partner.getEmail());
        response.setPhone(partner.getPhone());
        response.setStatus(partner.getStatus());
        response.setAvailability(partner.getAvailability());
        response.setVehicleType(partner.getVehicleType());
        response.setVehicleNumber(partner.getVehicleNumber());
        response.setDrivingLicense(partner.getDrivingLicense());
        response.setCity(partner.getCity());
        response.setCurrentLocation(partner.getCurrentLocation());
        response.setRating(partner.getRating());
        response.setTotalDeliveries(partner.getTotalDeliveries());
        response.setTotalEarnings(partner.getTotalEarnings());
        response.setProfilePhoto(partner.getProfilePhoto());
        response.setAddress(partner.getAddress());
        response.setCreatedAt(partner.getCreatedAt());
        response.setUpdatedAt(partner.getUpdatedAt());
        return response;
    }
}
