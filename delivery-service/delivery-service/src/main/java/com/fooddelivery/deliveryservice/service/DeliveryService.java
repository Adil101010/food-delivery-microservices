package com.fooddelivery.deliveryservice.service;

import com.fooddelivery.deliveryservice.dto.AssignDeliveryRequest;
import com.fooddelivery.deliveryservice.dto.DeliveryResponse;
import com.fooddelivery.deliveryservice.entity.Delivery;
import com.fooddelivery.deliveryservice.enums.DeliveryStatus;
import com.fooddelivery.deliveryservice.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryPartnerService partnerService;

    @Transactional
    public DeliveryResponse createPendingDelivery(Long orderId) {
        Optional<Delivery> existing = deliveryRepository.findByOrderId(orderId);
        if (existing.isPresent()) {
            log.info("⚠️ Delivery already exists for order: {}", orderId);
            return convertToResponse(existing.get());
        }

        Delivery delivery = new Delivery();
        delivery.setOrderId(orderId);
        delivery.setStatus(DeliveryStatus.PENDING.name()); // ✅ .name()

        Delivery saved = deliveryRepository.save(delivery);
        log.info("✅ Pending delivery created for order: {}", orderId);
        return convertToResponse(saved);
    }

    @Transactional
    public DeliveryResponse assignDelivery(AssignDeliveryRequest request) {
        if (deliveryRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new RuntimeException("Delivery already assigned for this order");
        }

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
        delivery.setEstimatedTime(
                request.getEstimatedTime() != null ? request.getEstimatedTime() : 30
        );
        delivery.setPartnerEarning(partnerEarning);
        delivery.setStatus(DeliveryStatus.ASSIGNED.name()); // ✅ .name()
        delivery.setAssignedAt(LocalDateTime.now());

        return convertToResponse(deliveryRepository.save(delivery));
    }

    @Transactional
    public DeliveryResponse acceptDelivery(Long deliveryId) {
        Delivery delivery = findById(deliveryId);
        if (!delivery.getStatus().equals(DeliveryStatus.ASSIGNED.name())) { // ✅ .equals()
            throw new RuntimeException("Delivery cannot be accepted in current status");
        }
        delivery.setStatus(DeliveryStatus.ACCEPTED.name()); // ✅ .name()
        delivery.setAcceptedAt(LocalDateTime.now());
        return convertToResponse(deliveryRepository.save(delivery));
    }

    @Transactional
    public DeliveryResponse rejectDelivery(Long deliveryId, String reason) {
        Delivery delivery = findById(deliveryId);
        if (!delivery.getStatus().equals(DeliveryStatus.ASSIGNED.name())) { // ✅ .equals()
            throw new RuntimeException("Delivery cannot be rejected in current status");
        }
        delivery.setStatus(DeliveryStatus.REJECTED.name()); // ✅ .name()
        delivery.setRejectionReason(reason);
        return convertToResponse(deliveryRepository.save(delivery));
    }

    @Transactional
    public DeliveryResponse markPickedUp(Long deliveryId) {
        Delivery delivery = findById(deliveryId);
        delivery.setStatus(DeliveryStatus.PICKED_UP.name()); // ✅ .name()
        delivery.setPickedUpAt(LocalDateTime.now());
        return convertToResponse(deliveryRepository.save(delivery));
    }

    @Transactional
    public DeliveryResponse markInTransit(Long deliveryId) {
        Delivery delivery = findById(deliveryId);
        delivery.setStatus(DeliveryStatus.IN_TRANSIT.name()); // ✅ .name()
        return convertToResponse(deliveryRepository.save(delivery));
    }

    @Transactional
    public DeliveryResponse markDelivered(Long deliveryId) {
        Delivery delivery = findById(deliveryId);
        delivery.setStatus(DeliveryStatus.DELIVERED.name()); // ✅ .name()
        delivery.setDeliveredAt(LocalDateTime.now());
        deliveryRepository.save(delivery);

        if (delivery.getPartnerId() != null) {
            partnerService.updatePartnerStats(
                    delivery.getPartnerId(),
                    delivery.getPartnerEarning(),
                    5.0
            );
        }

        return convertToResponse(delivery);
    }

    public DeliveryResponse getDeliveryById(Long id) {
        return convertToResponse(findById(id));
    }

    public DeliveryResponse getDeliveryByOrderId(Long orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery not found for order: " + orderId));
        return convertToResponse(delivery);
    }

    public List<DeliveryResponse> getPartnerDeliveries(Long partnerId) {
        return deliveryRepository.findByPartnerId(partnerId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<DeliveryResponse> getCustomerDeliveries(Long customerId) {
        return deliveryRepository.findByCustomerId(customerId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private Delivery findById(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + id));
    }

    private DeliveryResponse convertToResponse(Delivery delivery) {
        DeliveryResponse response = new DeliveryResponse();
        response.setId(delivery.getId());
        response.setOrderId(delivery.getOrderId());
        response.setPartnerId(delivery.getPartnerId());
        response.setRestaurantId(delivery.getRestaurantId());
        response.setCustomerId(delivery.getCustomerId());
        response.setStatus(delivery.getStatus()); // ✅ String as-is
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
