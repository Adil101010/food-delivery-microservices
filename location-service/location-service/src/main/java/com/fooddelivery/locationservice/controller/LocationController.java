package com.fooddelivery.locationservice.controller;

import com.fooddelivery.locationservice.dto.*;
import com.fooddelivery.locationservice.entity.LocationHistory;
import com.fooddelivery.locationservice.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LocationController {

    private final LocationService locationService;

    // Health Check
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        return ResponseEntity.ok(new MessageResponse("Location Service is running"));
    }

    // Update Partner Location
    @PostMapping("/update")
    public ResponseEntity<LocationResponse> updateLocation(
            @Valid @RequestBody UpdateLocationRequest request) {
        LocationResponse location = locationService.updateLocation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(location);
    }

    // Get Partner Current Location
    @GetMapping("/partner/{partnerId}")
    public ResponseEntity<LocationResponse> getPartnerLocation(@PathVariable Long partnerId) {
        LocationResponse location = locationService.getPartnerLocation(partnerId);
        return ResponseEntity.ok(location);
    }

    // Calculate Distance
    @PostMapping("/distance")
    public ResponseEntity<DistanceResponse> calculateDistance(
            @Valid @RequestBody DistanceRequest request) {
        DistanceResponse distance = locationService.calculateDistance(request);
        return ResponseEntity.ok(distance);
    }

    // Find Nearby Partners
    @PostMapping("/nearby")
    public ResponseEntity<List<NearbyPartnerResponse>> findNearbyPartners(
            @Valid @RequestBody NearbyPartnersRequest request) {
        List<NearbyPartnerResponse> partners = locationService.findNearbyPartners(request);
        return ResponseEntity.ok(partners);
    }

    // Get All Online Partners
    @GetMapping("/online")
    public ResponseEntity<List<LocationResponse>> getAllOnlinePartners() {
        List<LocationResponse> partners = locationService.getAllOnlinePartners();
        return ResponseEntity.ok(partners);
    }

    // Get Partner Location History
    @GetMapping("/history/partner/{partnerId}")
    public ResponseEntity<List<LocationHistory>> getPartnerHistory(@PathVariable Long partnerId) {
        List<LocationHistory> history = locationService.getPartnerHistory(partnerId);
        return ResponseEntity.ok(history);
    }

    // Get Delivery Location History
    @GetMapping("/history/delivery/{deliveryId}")
    public ResponseEntity<List<LocationHistory>> getDeliveryHistory(@PathVariable Long deliveryId) {
        List<LocationHistory> history = locationService.getDeliveryHistory(deliveryId);
        return ResponseEntity.ok(history);
    }
}
