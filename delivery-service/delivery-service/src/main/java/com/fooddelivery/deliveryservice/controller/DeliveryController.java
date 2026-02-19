package com.fooddelivery.deliveryservice.controller;

import com.fooddelivery.deliveryservice.dto.AssignDeliveryRequest;
import com.fooddelivery.deliveryservice.dto.DeliveryResponse;
import com.fooddelivery.deliveryservice.dto.MessageResponse;
import com.fooddelivery.deliveryservice.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DeliveryController {

    private final DeliveryService deliveryService;

    // Health Check
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        return ResponseEntity.ok(new MessageResponse("Delivery Service is running"));
    }

    // Assign Delivery
    @PostMapping("/assign")
    public ResponseEntity<DeliveryResponse> assignDelivery(
            @Valid @RequestBody AssignDeliveryRequest request) {
        DeliveryResponse delivery = deliveryService.assignDelivery(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(delivery);
    }

    // Accept Delivery
    @PostMapping("/{deliveryId}/accept")
    public ResponseEntity<DeliveryResponse> acceptDelivery(@PathVariable Long deliveryId) {
        DeliveryResponse delivery = deliveryService.acceptDelivery(deliveryId);
        return ResponseEntity.ok(delivery);
    }

    // Reject Delivery
    @PostMapping("/{deliveryId}/reject")
    public ResponseEntity<DeliveryResponse> rejectDelivery(
            @PathVariable Long deliveryId,
            @RequestParam String reason) {
        DeliveryResponse delivery = deliveryService.rejectDelivery(deliveryId, reason);
        return ResponseEntity.ok(delivery);
    }

    // Mark Picked Up
    @PostMapping("/{deliveryId}/pickup")
    public ResponseEntity<DeliveryResponse> markPickedUp(@PathVariable Long deliveryId) {
        DeliveryResponse delivery = deliveryService.markPickedUp(deliveryId);
        return ResponseEntity.ok(delivery);
    }

    // Mark In Transit
    @PostMapping("/{deliveryId}/transit")
    public ResponseEntity<DeliveryResponse> markInTransit(@PathVariable Long deliveryId) {
        DeliveryResponse delivery = deliveryService.markInTransit(deliveryId);
        return ResponseEntity.ok(delivery);
    }

    // Mark Delivered
    @PostMapping("/{deliveryId}/delivered")
    public ResponseEntity<DeliveryResponse> markDelivered(@PathVariable Long deliveryId) {
        DeliveryResponse delivery = deliveryService.markDelivered(deliveryId);
        return ResponseEntity.ok(delivery);
    }

    // Get Delivery by ID
    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponse> getDeliveryById(@PathVariable Long id) {
        DeliveryResponse delivery = deliveryService.getDeliveryById(id);
        return ResponseEntity.ok(delivery);
    }

    // Get Delivery by Order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryResponse> getDeliveryByOrderId(@PathVariable Long orderId) {
        DeliveryResponse delivery = deliveryService.getDeliveryByOrderId(orderId);
        return ResponseEntity.ok(delivery);
    }

    // Get Partner Deliveries
    @GetMapping("/partner/{partnerId}")
    public ResponseEntity<List<DeliveryResponse>> getPartnerDeliveries(@PathVariable Long partnerId) {
        List<DeliveryResponse> deliveries = deliveryService.getPartnerDeliveries(partnerId);
        return ResponseEntity.ok(deliveries);
    }

    // Get Customer Deliveries
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<DeliveryResponse>> getCustomerDeliveries(@PathVariable Long customerId) {
        List<DeliveryResponse> deliveries = deliveryService.getCustomerDeliveries(customerId);
        return ResponseEntity.ok(deliveries);
    }
    // ✅ Order service ya manually call kar sake
    @PostMapping("/order/{orderId}/create-pending")
    public ResponseEntity<DeliveryResponse> createPendingDelivery(
            @PathVariable Long orderId) {
        DeliveryResponse delivery = deliveryService.createPendingDelivery(orderId);
        return ResponseEntity.ok(delivery);
    }

}
