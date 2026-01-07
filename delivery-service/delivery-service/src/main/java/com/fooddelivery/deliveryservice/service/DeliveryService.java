package com.fooddelivery.deliveryservice.service;

import com.fooddelivery.deliveryservice.dto.AssignDeliveryRequest;
import com.fooddelivery.deliveryservice.dto.DeliveryResponse;
import com.fooddelivery.deliveryservice.entity.Delivery;
import com.fooddelivery.deliveryservice.enums.DeliveryStatus;
import com.fooddelivery.deliveryservice.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryPartnerService partnerService;

    // Assign Delivery to Partner
    @Transactional
    public DeliveryResponse assignDelivery(AssignDeliveryRequest request) {

        // Check if order already has delivery assigned
        if (deliveryRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new RuntimeException("Delivery already assigned for this order");
        }

        // Calculate partner earning (70% of delivery fee)
        Double partnerEarning = request.getDeliveryFee() * 0.7;

        Delivery delivery = new Delivery();
        delivery.setOrderId(request.getOrderId());
        delivery.setPartnerId(request.getPartnerId());
        delivery.setRestaurantId(request.getRestaurantId());
        delivery.setCustomerId(request.getCustomerId());
        delivery.setPickupAddress(request.getPickupAddress());
        delivery.setDeliveryAddress(request.getDeliveryAddress());
        delivery.setDeliveryFee(request.getDeliveryFee());
        delivery.setDistance(request.getDistance());
        delivery.setCustomerPhone(request.getCustomerPhone());
        delivery.setDeliveryInstructions(request.getDeliveryInstructions());
        delivery.setEstimatedTime(request.getEstimatedTime() != null ? request.getEstimatedTime() : 30);
        delivery.setPartnerEarning(partnerEarning);
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setAssignedAt(LocalDateTime.now());

        Delivery savedDelivery = deliveryRepository.save(delivery);
        return convertToResponse(savedDelivery);
    }

    // Accept Delivery
    @Transactional
    public DeliveryResponse acceptDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (delivery.getStatus() != DeliveryStatus.ASSIGNED) {
            throw new RuntimeException("Delivery cannot be accepted in current status");
        }

        delivery.setStatus(DeliveryStatus.ACCEPTED);
        delivery.setAcceptedAt(LocalDateTime.now());

        Delivery updatedDelivery = deliveryRepository.save(delivery);
        return convertToResponse(updatedDelivery);
    }

    // Reject Delivery
    @Transactional
    public DeliveryResponse rejectDelivery(Long deliveryId, String reason) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (delivery.getStatus() != DeliveryStatus.ASSIGNED) {
            throw new RuntimeException("Delivery cannot be rejected in current status");
        }

        delivery.setStatus(DeliveryStatus.REJECTED);
        delivery.setRejectionReason(reason);

        Delivery updatedDelivery = deliveryRepository.save(delivery);
        return convertToResponse(updatedDelivery);
    }

    // Mark Picked Up
    @Transactional
    public DeliveryResponse markPickedUp(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        delivery.setStatus(DeliveryStatus.PICKED_UP);
        delivery.setPickedUpAt(LocalDateTime.now());

        Delivery updatedDelivery = deliveryRepository.save(delivery);
        return convertToResponse(updatedDelivery);
    }

    // Mark In Transit
    @Transactional
    public DeliveryResponse markInTransit(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        delivery.setStatus(DeliveryStatus.IN_TRANSIT);

        Delivery updatedDelivery = deliveryRepository.save(delivery);
        return convertToResponse(updatedDelivery);
    }

    // Mark Delivered
    @Transactional
    public DeliveryResponse markDelivered(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredAt(LocalDateTime.now());

        Delivery updatedDelivery = deliveryRepository.save(delivery);

        // Update partner stats
        partnerService.updatePartnerStats(
                delivery.getPartnerId(),
                delivery.getPartnerEarning(),
                5.0 // Default rating, can be updated later
        );

        return convertToResponse(updatedDelivery);
    }

    // Get Delivery by ID
    public DeliveryResponse getDeliveryById(Long id) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
        return convertToResponse(delivery);
    }

    // Get Delivery by Order ID
    public DeliveryResponse getDeliveryByOrderId(Long orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery not found for order"));
        return convertToResponse(delivery);
    }

    // Get Partner Deliveries
    public List<DeliveryResponse> getPartnerDeliveries(Long partnerId) {
        List<Delivery> deliveries = deliveryRepository.findByPartnerId(partnerId);
        return deliveries.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get Customer Deliveries
    public List<DeliveryResponse> getCustomerDeliveries(Long customerId) {
        List<Delivery> deliveries = deliveryRepository.findByCustomerId(customerId);
        return deliveries.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Convert Entity to Response
    private DeliveryResponse convertToResponse(Delivery delivery) {
        DeliveryResponse response = new DeliveryResponse();
        response.setId(delivery.getId());
        response.setOrderId(delivery.getOrderId());
        response.setPartnerId(delivery.getPartnerId());
        response.setRestaurantId(delivery.getRestaurantId());
        response.setCustomerId(delivery.getCustomerId());
        response.setStatus(delivery.getStatus());
        response.setPickupAddress(delivery.getPickupAddress());
        response.setDeliveryAddress(delivery.getDeliveryAddress());
        response.setDeliveryFee(delivery.getDeliveryFee());
        response.setDistance(delivery.getDistance());
        response.setCustomerPhone(delivery.getCustomerPhone());
        response.setDeliveryInstructions(delivery.getDeliveryInstructions());
        response.setAssignedAt(delivery.getAssignedAt());
        response.setAcceptedAt(delivery.getAcceptedAt());
        response.setPickedUpAt(delivery.getPickedUpAt());
        response.setDeliveredAt(delivery.getDeliveredAt());
        response.setEstimatedTime(delivery.getEstimatedTime());
        response.setRejectionReason(delivery.getRejectionReason());
        response.setPartnerEarning(delivery.getPartnerEarning());
        response.setCreatedAt(delivery.getCreatedAt());
        response.setUpdatedAt(delivery.getUpdatedAt());
        return response;
    }
}
