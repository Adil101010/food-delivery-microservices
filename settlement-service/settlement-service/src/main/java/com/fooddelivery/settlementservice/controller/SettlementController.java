package com.fooddelivery.settlementservice.controller;

import com.fooddelivery.settlementservice.dto.*;
import com.fooddelivery.settlementservice.enums.EntityType;
import com.fooddelivery.settlementservice.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settlement")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SettlementController {

    private final SettlementService settlementService;

    // Health Check
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        return ResponseEntity.ok(new MessageResponse("Settlement Service is running"));
    }

    // Calculate Settlement (Auto)
    @PostMapping("/calculate")
    public ResponseEntity<SettlementResponse> calculateSettlement(
            @Valid @RequestBody CalculateSettlementRequest request) {
        SettlementResponse settlement = settlementService.calculateSettlement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(settlement);
    }

    // Create Settlement (Manual)
    @PostMapping
    public ResponseEntity<SettlementResponse> createSettlement(
            @Valid @RequestBody CreateSettlementRequest request) {
        SettlementResponse settlement = settlementService.createSettlement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(settlement);
    }

    // Get Settlement by ID
    @GetMapping("/{id}")
    public ResponseEntity<SettlementResponse> getSettlement(@PathVariable Long id) {
        SettlementResponse settlement = settlementService.getSettlement(id);
        return ResponseEntity.ok(settlement);
    }

    // Get Settlement with Payout
    @GetMapping("/{id}/with-payout")
    public ResponseEntity<SettlementWithPayoutResponse> getSettlementWithPayout(@PathVariable Long id) {
        SettlementWithPayoutResponse response = settlementService.getSettlementWithPayout(id);
        return ResponseEntity.ok(response);
    }

    // Get Settlements by Restaurant
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<SettlementResponse>> getRestaurantSettlements(
            @PathVariable Long restaurantId) {
        List<SettlementResponse> settlements = settlementService
                .getSettlementsByEntity(EntityType.RESTAURANT, restaurantId);
        return ResponseEntity.ok(settlements);
    }

    // Get Settlements by Delivery Partner
    @GetMapping("/partner/{partnerId}")
    public ResponseEntity<List<SettlementResponse>> getPartnerSettlements(
            @PathVariable Long partnerId) {
        List<SettlementResponse> settlements = settlementService
                .getSettlementsByEntity(EntityType.DELIVERY_PARTNER, partnerId);
        return ResponseEntity.ok(settlements);
    }

    // Get Pending Settlements
    @GetMapping("/pending")
    public ResponseEntity<List<SettlementResponse>> getPendingSettlements() {
        List<SettlementResponse> settlements = settlementService.getPendingSettlements();
        return ResponseEntity.ok(settlements);
    }

    // Update Settlement Status
    @PutMapping("/{id}/status")
    public ResponseEntity<SettlementResponse> updateSettlementStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSettlementStatusRequest request) {
        SettlementResponse settlement = settlementService.updateSettlementStatus(id, request);
        return ResponseEntity.ok(settlement);
    }

    // Process Payout
    @PostMapping("/payout")
    public ResponseEntity<PayoutResponse> processPayout(
            @Valid @RequestBody ProcessPayoutRequest request) {
        PayoutResponse payout = settlementService.processPayout(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payout);
    }

    // Update Payout Status
    @PutMapping("/payout/{id}/status")
    public ResponseEntity<PayoutResponse> updatePayoutStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePayoutStatusRequest request) {
        PayoutResponse payout = settlementService.updatePayoutStatus(id, request);
        return ResponseEntity.ok(payout);
    }

    // Get Settlement Summary for Restaurant
    @GetMapping("/summary/restaurant/{restaurantId}")
    public ResponseEntity<SettlementSummaryResponse> getRestaurantSummary(
            @PathVariable Long restaurantId) {
        SettlementSummaryResponse summary = settlementService
                .getSettlementSummary(EntityType.RESTAURANT, restaurantId);
        return ResponseEntity.ok(summary);
    }

    // Get Settlement Summary for Delivery Partner
    @GetMapping("/summary/partner/{partnerId}")
    public ResponseEntity<SettlementSummaryResponse> getPartnerSummary(
            @PathVariable Long partnerId) {
        SettlementSummaryResponse summary = settlementService
                .getSettlementSummary(EntityType.DELIVERY_PARTNER, partnerId);
        return ResponseEntity.ok(summary);
    }
}
