package com.fooddelivery.assignmentservice.service;

import com.fooddelivery.assignmentservice.dto.*;
import com.fooddelivery.assignmentservice.entity.Assignment;
import com.fooddelivery.assignmentservice.enums.AssignmentStatus;
import com.fooddelivery.assignmentservice.enums.AssignmentType;
import com.fooddelivery.assignmentservice.repository.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final RestTemplate restTemplate;

    @Value("${location.service.url}")
    private String locationServiceUrl;

    // Auto Assignment (Find nearest partner)
    @Transactional
    public AssignmentResponse autoAssign(AutoAssignRequest request) {

        // Check if order already assigned
        if (assignmentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new RuntimeException("Order already assigned");
        }

        // Create assignment record
        Assignment assignment = new Assignment();
        assignment.setOrderId(request.getOrderId());
        assignment.setRestaurantId(request.getRestaurantId());
        assignment.setCustomerId(request.getCustomerId());
        assignment.setRestaurantLatitude(request.getRestaurantLatitude());
        assignment.setRestaurantLongitude(request.getRestaurantLongitude());
        assignment.setCustomerLatitude(request.getCustomerLatitude());
        assignment.setCustomerLongitude(request.getCustomerLongitude());
        assignment.setType(AssignmentType.AUTO);
        assignment.setStatus(AssignmentStatus.PENDING);
        assignment.setAttemptCount(1);

        Assignment savedAssignment = assignmentRepository.save(assignment);

        // Find nearby partners using Location Service
        Double radiusKm = request.getSearchRadiusKm() != null ? request.getSearchRadiusKm() : 5.0;
        List<NearbyPartnerDTO> nearbyPartners = findNearbyPartners(
                request.getRestaurantLatitude(),
                request.getRestaurantLongitude(),
                radiusKm
        );

        if (nearbyPartners.isEmpty()) {
            log.warn("No partners found within {} km radius", radiusKm);
            savedAssignment.setStatus(AssignmentStatus.PENDING);
            assignmentRepository.save(savedAssignment);
            return convertToResponse(savedAssignment);
        }

        // Get first available partner (closest one)
        NearbyPartnerDTO selectedPartner = nearbyPartners.stream()
                .filter(p -> p.getCurrentDeliveryId() == null) // Not busy
                .findFirst()
                .orElse(nearbyPartners.get(0)); // If all busy, take closest

        // Update assignment
        savedAssignment.setPartnerId(selectedPartner.getPartnerId());
        savedAssignment.setDistance(selectedPartner.getDistanceKm());
        savedAssignment.setEstimatedTime(selectedPartner.getEtaMinutes());
        savedAssignment.setStatus(AssignmentStatus.ASSIGNED);
        savedAssignment.setAssignedAt(LocalDateTime.now());

        Assignment finalAssignment = assignmentRepository.save(savedAssignment);

        log.info("Order {} auto-assigned to partner {}", request.getOrderId(), selectedPartner.getPartnerId());

        return convertToResponse(finalAssignment);
    }

    // Manual Assignment (Admin override)
    @Transactional
    public AssignmentResponse manualAssign(ManualAssignRequest request) {

        // Check if order already assigned
        Assignment existingAssignment = assignmentRepository.findByOrderId(request.getOrderId())
                .orElse(null);

        Assignment assignment;

        if (existingAssignment != null) {
            // Reassignment case
            assignment = existingAssignment;
            assignment.setType(AssignmentType.MANUAL);
            assignment.setStatus(AssignmentStatus.REASSIGNED);
            assignment.setAttemptCount(assignment.getAttemptCount() + 1);
        } else {
            // New assignment
            assignment = new Assignment();
            assignment.setOrderId(request.getOrderId());
            assignment.setRestaurantId(request.getRestaurantId());
            assignment.setCustomerId(request.getCustomerId());
            assignment.setRestaurantLatitude(request.getRestaurantLatitude());
            assignment.setRestaurantLongitude(request.getRestaurantLongitude());
            assignment.setCustomerLatitude(request.getCustomerLatitude());
            assignment.setCustomerLongitude(request.getCustomerLongitude());
            assignment.setType(AssignmentType.MANUAL);
            assignment.setStatus(AssignmentStatus.ASSIGNED);
            assignment.setAttemptCount(1);
        }

        assignment.setPartnerId(request.getPartnerId());
        assignment.setAssignedAt(LocalDateTime.now());

        // Calculate distance (mock calculation for now)
        assignment.setDistance(5.0); // You can call location service here
        assignment.setEstimatedTime(30);

        Assignment savedAssignment = assignmentRepository.save(assignment);

        log.info("Order {} manually assigned to partner {}", request.getOrderId(), request.getPartnerId());

        return convertToResponse(savedAssignment);
    }

    // Accept Assignment
    @Transactional
    public AssignmentResponse acceptAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        assignment.setStatus(AssignmentStatus.ACCEPTED);
        assignment.setAcceptedAt(LocalDateTime.now());

        Assignment updated = assignmentRepository.save(assignment);
        return convertToResponse(updated);
    }

    // Reject Assignment
    @Transactional
    public AssignmentResponse rejectAssignment(Long assignmentId, String reason) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        assignment.setStatus(AssignmentStatus.REJECTED);
        assignment.setRejectionReason(reason);

        Assignment updated = assignmentRepository.save(assignment);
        return convertToResponse(updated);
    }

    // Get Assignment by Order ID
    public AssignmentResponse getAssignmentByOrderId(Long orderId) {
        Assignment assignment = assignmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Assignment not found for order"));
        return convertToResponse(assignment);
    }

    // Get Partner Assignments
    public List<AssignmentResponse> getPartnerAssignments(Long partnerId) {
        List<Assignment> assignments = assignmentRepository.findByPartnerId(partnerId);
        return assignments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Find Nearby Partners (Call Location Service)
    private List<NearbyPartnerDTO> findNearbyPartners(Double latitude, Double longitude, Double radiusKm) {
        try {
            String url = locationServiceUrl + "/api/locations/nearby";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("latitude", latitude);
            requestBody.put("longitude", longitude);
            requestBody.put("radiusKm", radiusKm);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody);

            ResponseEntity<List<NearbyPartnerDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<List<NearbyPartnerDTO>>() {}
            );

            return response.getBody();

        } catch (Exception e) {
            log.error("Error calling Location Service: {}", e.getMessage());
            return List.of(); // Return empty list on error
        }
    }

    // Convert Entity to Response
    private AssignmentResponse convertToResponse(Assignment assignment) {
        AssignmentResponse response = new AssignmentResponse();
        response.setId(assignment.getId());
        response.setOrderId(assignment.getOrderId());
        response.setRestaurantId(assignment.getRestaurantId());
        response.setCustomerId(assignment.getCustomerId());
        response.setPartnerId(assignment.getPartnerId());
        response.setStatus(assignment.getStatus());
        response.setType(assignment.getType());
        response.setDistance(assignment.getDistance());
        response.setEstimatedTime(assignment.getEstimatedTime());
        response.setAttemptCount(assignment.getAttemptCount());
        response.setRejectionReason(assignment.getRejectionReason());
        response.setAssignedAt(assignment.getAssignedAt());
        response.setAcceptedAt(assignment.getAcceptedAt());
        response.setCompletedAt(assignment.getCompletedAt());
        response.setCreatedAt(assignment.getCreatedAt());
        response.setUpdatedAt(assignment.getUpdatedAt());
        return response;
    }
}
