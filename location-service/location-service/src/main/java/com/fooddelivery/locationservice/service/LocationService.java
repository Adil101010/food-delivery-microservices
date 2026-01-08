package com.fooddelivery.locationservice.service;

import com.fooddelivery.locationservice.dto.*;
import com.fooddelivery.locationservice.entity.LocationHistory;
import com.fooddelivery.locationservice.entity.PartnerLocation;
import com.fooddelivery.locationservice.repository.LocationHistoryRepository;
import com.fooddelivery.locationservice.repository.PartnerLocationRepository;
import com.fooddelivery.locationservice.util.LocationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final PartnerLocationRepository locationRepository;
    private final LocationHistoryRepository historyRepository;

    // Update Partner Location
    @Transactional
    public LocationResponse updateLocation(UpdateLocationRequest request) {

        PartnerLocation location = locationRepository.findByPartnerId(request.getPartnerId())
                .orElse(new PartnerLocation());

        location.setPartnerId(request.getPartnerId());
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setSpeed(request.getSpeed());
        location.setHeading(request.getHeading());
        location.setAccuracy(request.getAccuracy());
        location.setIsMoving(request.getIsMoving() != null ? request.getIsMoving() : false);
        location.setIsOnline(request.getIsOnline() != null ? request.getIsOnline() : true);
        location.setCurrentDeliveryId(request.getCurrentDeliveryId());

        PartnerLocation savedLocation = locationRepository.save(location);

        // Save to history
        saveToHistory(savedLocation);

        return convertToResponse(savedLocation);
    }

    // Get Partner Current Location
    public LocationResponse getPartnerLocation(Long partnerId) {
        PartnerLocation location = locationRepository.findByPartnerId(partnerId)
                .orElseThrow(() -> new RuntimeException("Location not found for partner"));
        return convertToResponse(location);
    }

    // Calculate Distance between two points
    public DistanceResponse calculateDistance(DistanceRequest request) {

        double distance = LocationUtils.calculateDistance(
                request.getStartLatitude(),
                request.getStartLongitude(),
                request.getEndLatitude(),
                request.getEndLongitude()
        );

        int eta = LocationUtils.calculateETA(distance, 30.0); // 30 km/h average speed

        String message = String.format("Distance: %.2f km, ETA: %d minutes", distance, eta);

        return new DistanceResponse(distance, eta, message);
    }

    // Find Nearby Partners
    public List<NearbyPartnerResponse> findNearbyPartners(NearbyPartnersRequest request) {

        // Calculate bounding box for rough filtering
        double latDelta = request.getRadiusKm() / 111.0; // 1 degree ≈ 111 km
        double lonDelta = request.getRadiusKm() / (111.0 * Math.cos(Math.toRadians(request.getLatitude())));

        double minLat = request.getLatitude() - latDelta;
        double maxLat = request.getLatitude() + latDelta;
        double minLon = request.getLongitude() - lonDelta;
        double maxLon = request.getLongitude() + lonDelta;

        // Get partners in area
        List<PartnerLocation> partnersInArea = locationRepository.findPartnersInArea(
                minLat, maxLat, minLon, maxLon
        );

        // Calculate exact distance and filter
        List<NearbyPartnerResponse> nearbyPartners = new ArrayList<>();

        for (PartnerLocation partner : partnersInArea) {
            double distance = LocationUtils.calculateDistance(
                    request.getLatitude(),
                    request.getLongitude(),
                    partner.getLatitude(),
                    partner.getLongitude()
            );

            if (distance <= request.getRadiusKm()) {
                int eta = LocationUtils.calculateETA(distance, 30.0);

                NearbyPartnerResponse response = new NearbyPartnerResponse();
                response.setPartnerId(partner.getPartnerId());
                response.setLatitude(partner.getLatitude());
                response.setLongitude(partner.getLongitude());
                response.setDistanceKm(distance);
                response.setEtaMinutes(eta);
                response.setIsMoving(partner.getIsMoving());
                response.setCurrentDeliveryId(partner.getCurrentDeliveryId());

                nearbyPartners.add(response);
            }
        }

        // Sort by distance (closest first)
        nearbyPartners.sort(Comparator.comparing(NearbyPartnerResponse::getDistanceKm));

        return nearbyPartners;
    }

    // Get All Online Partners
    public List<LocationResponse> getAllOnlinePartners() {
        List<PartnerLocation> partners = locationRepository.findByIsOnline(true);
        return partners.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get Partner Location History
    public List<LocationHistory> getPartnerHistory(Long partnerId) {
        return historyRepository.findByPartnerId(partnerId);
    }

    // Get Delivery Location History
    public List<LocationHistory> getDeliveryHistory(Long deliveryId) {
        return historyRepository.findByDeliveryId(deliveryId);
    }

    // Save location to history
    private void saveToHistory(PartnerLocation location) {
        LocationHistory history = new LocationHistory();
        history.setPartnerId(location.getPartnerId());
        history.setLatitude(location.getLatitude());
        history.setLongitude(location.getLongitude());
        history.setSpeed(location.getSpeed());
        history.setHeading(location.getHeading());
        history.setDeliveryId(location.getCurrentDeliveryId());
        history.setTimestamp(LocalDateTime.now());

        historyRepository.save(history);
    }

    // Convert Entity to Response
    private LocationResponse convertToResponse(PartnerLocation location) {
        LocationResponse response = new LocationResponse();
        response.setId(location.getId());
        response.setPartnerId(location.getPartnerId());
        response.setLatitude(location.getLatitude());
        response.setLongitude(location.getLongitude());
        response.setSpeed(location.getSpeed());
        response.setHeading(location.getHeading());
        response.setAccuracy(location.getAccuracy());
        response.setIsMoving(location.getIsMoving());
        response.setIsOnline(location.getIsOnline());
        response.setCurrentDeliveryId(location.getCurrentDeliveryId());
        response.setUpdatedAt(location.getUpdatedAt());
        return response;
    }
}
