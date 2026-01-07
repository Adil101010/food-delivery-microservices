package com.fooddelivery.deliveryservice.controller;

import com.fooddelivery.deliveryservice.dto.DeliveryPartnerRequest;
import com.fooddelivery.deliveryservice.dto.DeliveryPartnerResponse;
import com.fooddelivery.deliveryservice.dto.MessageResponse;
import com.fooddelivery.deliveryservice.enums.AvailabilityStatus;
import com.fooddelivery.deliveryservice.enums.PartnerStatus;
import com.fooddelivery.deliveryservice.service.DeliveryPartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery/partners")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DeliveryPartnerController {

    private final DeliveryPartnerService partnerService;

    // Health Check
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        return ResponseEntity.ok(new MessageResponse("Delivery Partner Service is running"));
    }

    // Register Partner
    @PostMapping("/register")
    public ResponseEntity<DeliveryPartnerResponse> registerPartner(
            @Valid @RequestBody DeliveryPartnerRequest request) {
        DeliveryPartnerResponse partner = partnerService.registerPartner(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(partner);
    }

    // Get Partner by ID
    @GetMapping("/{id}")
    public ResponseEntity<DeliveryPartnerResponse> getPartnerById(@PathVariable Long id) {
        DeliveryPartnerResponse partner = partnerService.getPartnerById(id);
        return ResponseEntity.ok(partner);
    }

    // Get Partner by User ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<DeliveryPartnerResponse> getPartnerByUserId(@PathVariable Long userId) {
        DeliveryPartnerResponse partner = partnerService.getPartnerByUserId(userId);
        return ResponseEntity.ok(partner);
    }

    // Update Partner Profile
    @PutMapping("/{id}")
    public ResponseEntity<DeliveryPartnerResponse> updatePartner(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryPartnerRequest request) {
        DeliveryPartnerResponse partner = partnerService.updatePartner(id, request);
        return ResponseEntity.ok(partner);
    }

    // Update Partner Status (Admin)
    @PatchMapping("/{id}/status")
    public ResponseEntity<DeliveryPartnerResponse> updatePartnerStatus(
            @PathVariable Long id,
            @RequestParam PartnerStatus status) {
        DeliveryPartnerResponse partner = partnerService.updatePartnerStatus(id, status);
        return ResponseEntity.ok(partner);
    }

    // Update Availability (Go Online/Offline)
    @PatchMapping("/{id}/availability")
    public ResponseEntity<DeliveryPartnerResponse> updateAvailability(
            @PathVariable Long id,
            @RequestParam AvailabilityStatus availability) {
        DeliveryPartnerResponse partner = partnerService.updateAvailability(id, availability);
        return ResponseEntity.ok(partner);
    }

    // Get Available Partners in City
    @GetMapping("/available/{city}")
    public ResponseEntity<List<DeliveryPartnerResponse>> getAvailablePartnersInCity(
            @PathVariable String city) {
        List<DeliveryPartnerResponse> partners = partnerService.getAvailablePartnersInCity(city);
        return ResponseEntity.ok(partners);
    }

    // Get All Partners
    @GetMapping
    public ResponseEntity<List<DeliveryPartnerResponse>> getAllPartners() {
        List<DeliveryPartnerResponse> partners = partnerService.getAllPartners();
        return ResponseEntity.ok(partners);
    }
}
