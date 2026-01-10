package com.fooddelivery.settlementservice.service;

import com.fooddelivery.settlementservice.dto.*;
import com.fooddelivery.settlementservice.entity.Payout;
import com.fooddelivery.settlementservice.entity.Settlement;
import com.fooddelivery.settlementservice.enums.*;
import com.fooddelivery.settlementservice.repository.PayoutRepository;
import com.fooddelivery.settlementservice.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final PayoutRepository payoutRepository;

    // Calculate Settlement
    @Transactional
    public SettlementResponse calculateSettlement(CalculateSettlementRequest request) {
        // Check if settlement already exists for this period
        settlementRepository.findByEntityTypeAndEntityIdAndPeriodStartAndPeriodEnd(
                request.getEntityType(), request.getEntityId(),
                request.getPeriodStart(), request.getPeriodEnd()
        ).ifPresent(s -> {
            throw new RuntimeException("Settlement already exists for this period");
        });

        // Mock calculation - In real app, fetch from order service
        Integer totalOrders = 10; // Mock value
        BigDecimal grossAmount = new BigDecimal("5000.00"); // Mock value

        // Commission rates
        BigDecimal commissionRate = request.getEntityType() == EntityType.RESTAURANT
                ? new BigDecimal("15.00") // 15% for restaurants
                : new BigDecimal("20.00"); // 20% for delivery partners

        // Calculate commission and net amount
        BigDecimal commissionAmount = grossAmount.multiply(commissionRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal netAmount = grossAmount.subtract(commissionAmount);

        // Create settlement
        Settlement settlement = new Settlement();
        settlement.setEntityType(request.getEntityType());
        settlement.setEntityId(request.getEntityId());
        settlement.setPeriodStart(request.getPeriodStart());
        settlement.setPeriodEnd(request.getPeriodEnd());
        settlement.setTotalOrders(totalOrders);
        settlement.setGrossAmount(grossAmount);
        settlement.setCommissionRate(commissionRate);
        settlement.setCommissionAmount(commissionAmount);
        settlement.setNetAmount(netAmount);
        settlement.setStatus(SettlementStatus.PENDING);

        Settlement saved = settlementRepository.save(settlement);
        return mapToSettlementResponse(saved);
    }

    // Create Settlement (Manual)
    @Transactional
    public SettlementResponse createSettlement(CreateSettlementRequest request) {
        // Calculate commission and net amount
        BigDecimal commissionAmount = request.getGrossAmount()
                .multiply(request.getCommissionRate())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal netAmount = request.getGrossAmount().subtract(commissionAmount);

        Settlement settlement = new Settlement();
        settlement.setEntityType(request.getEntityType());
        settlement.setEntityId(request.getEntityId());
        settlement.setPeriodStart(request.getPeriodStart());
        settlement.setPeriodEnd(request.getPeriodEnd());
        settlement.setTotalOrders(request.getTotalOrders());
        settlement.setGrossAmount(request.getGrossAmount());
        settlement.setCommissionRate(request.getCommissionRate());
        settlement.setCommissionAmount(commissionAmount);
        settlement.setNetAmount(netAmount);
        settlement.setStatus(SettlementStatus.PENDING);

        Settlement saved = settlementRepository.save(settlement);
        return mapToSettlementResponse(saved);
    }

    // Get Settlement by ID
    public SettlementResponse getSettlement(Long id) {
        Settlement settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Settlement not found with id: " + id));
        return mapToSettlementResponse(settlement);
    }

    // Get Settlement with Payout
    public SettlementWithPayoutResponse getSettlementWithPayout(Long id) {
        Settlement settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Settlement not found with id: " + id));

        SettlementWithPayoutResponse response = new SettlementWithPayoutResponse();
        response.setSettlement(mapToSettlementResponse(settlement));

        payoutRepository.findBySettlementId(id).ifPresent(payout ->
                response.setPayout(mapToPayoutResponse(payout))
        );

        return response;
    }

    // Get Settlements by Entity
    public List<SettlementResponse> getSettlementsByEntity(EntityType entityType, Long entityId) {
        List<Settlement> settlements = settlementRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
        return settlements.stream()
                .map(this::mapToSettlementResponse)
                .collect(Collectors.toList());
    }

    // Get Pending Settlements
    public List<SettlementResponse> getPendingSettlements() {
        List<Settlement> settlements = settlementRepository.findByStatus(SettlementStatus.PENDING);
        return settlements.stream()
                .map(this::mapToSettlementResponse)
                .collect(Collectors.toList());
    }

    // Update Settlement Status
    @Transactional
    public SettlementResponse updateSettlementStatus(Long id, UpdateSettlementStatusRequest request) {
        Settlement settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Settlement not found with id: " + id));

        settlement.setStatus(request.getStatus());

        if (request.getStatus() == SettlementStatus.COMPLETED) {
            settlement.setProcessedAt(LocalDateTime.now());
        }

        Settlement updated = settlementRepository.save(settlement);
        return mapToSettlementResponse(updated);
    }

    // Process Payout
    @Transactional
    public PayoutResponse processPayout(ProcessPayoutRequest request) {
        Settlement settlement = settlementRepository.findById(request.getSettlementId())
                .orElseThrow(() -> new RuntimeException("Settlement not found"));

        if (settlement.getStatus() != SettlementStatus.PENDING) {
            throw new RuntimeException("Settlement is not in PENDING status");
        }

        // Check if payout already exists
        if (payoutRepository.findBySettlementId(request.getSettlementId()).isPresent()) {
            throw new RuntimeException("Payout already exists for this settlement");
        }

        // Create payout
        Payout payout = new Payout();
        payout.setSettlementId(settlement.getId());
        payout.setAmount(settlement.getNetAmount());
        payout.setPaymentMethod(request.getPaymentMethod());
        payout.setAccountDetails(request.getAccountDetails());
        payout.setStatus(PayoutStatus.INITIATED);
        payout.setInitiatedAt(LocalDateTime.now());
        payout.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // Update settlement status
        settlement.setStatus(SettlementStatus.PROCESSING);
        settlementRepository.save(settlement);

        Payout saved = payoutRepository.save(payout);
        return mapToPayoutResponse(saved);
    }

    // Update Payout Status
    @Transactional
    public PayoutResponse updatePayoutStatus(Long id, UpdatePayoutStatusRequest request) {
        Payout payout = payoutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payout not found with id: " + id));

        payout.setStatus(request.getStatus());

        if (request.getTransactionId() != null) {
            payout.setTransactionId(request.getTransactionId());
        }

        if (request.getStatus() == PayoutStatus.COMPLETED) {
            payout.setCompletedAt(LocalDateTime.now());

            // Update settlement status
            Settlement settlement = settlementRepository.findById(payout.getSettlementId())
                    .orElseThrow(() -> new RuntimeException("Settlement not found"));
            settlement.setStatus(SettlementStatus.COMPLETED);
            settlement.setProcessedAt(LocalDateTime.now());
            settlementRepository.save(settlement);
        }

        Payout updated = payoutRepository.save(payout);
        return mapToPayoutResponse(updated);
    }

    // Get Settlement Summary
    public SettlementSummaryResponse getSettlementSummary(EntityType entityType, Long entityId) {
        List<Settlement> settlements = settlementRepository
                .findByEntityTypeAndEntityId(entityType, entityId);

        SettlementSummaryResponse summary = new SettlementSummaryResponse();
        summary.setEntityId(entityId);
        summary.setTotalSettlements(settlements.size());

        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalCommission = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;
        int pending = 0;
        int completed = 0;

        for (Settlement s : settlements) {
            totalGross = totalGross.add(s.getGrossAmount());
            totalCommission = totalCommission.add(s.getCommissionAmount());
            totalNet = totalNet.add(s.getNetAmount());

            if (s.getStatus() == SettlementStatus.PENDING) pending++;
            if (s.getStatus() == SettlementStatus.COMPLETED) completed++;
        }

        summary.setTotalGrossAmount(totalGross);
        summary.setTotalCommission(totalCommission);
        summary.setTotalNetAmount(totalNet);
        summary.setPendingSettlements(pending);
        summary.setCompletedSettlements(completed);

        return summary;
    }

    // Helper: Map Settlement to Response
    private SettlementResponse mapToSettlementResponse(Settlement settlement) {
        SettlementResponse response = new SettlementResponse();
        response.setId(settlement.getId());
        response.setEntityType(settlement.getEntityType());
        response.setEntityId(settlement.getEntityId());
        response.setPeriodStart(settlement.getPeriodStart());
        response.setPeriodEnd(settlement.getPeriodEnd());
        response.setTotalOrders(settlement.getTotalOrders());
        response.setGrossAmount(settlement.getGrossAmount());
        response.setCommissionRate(settlement.getCommissionRate());
        response.setCommissionAmount(settlement.getCommissionAmount());
        response.setNetAmount(settlement.getNetAmount());
        response.setStatus(settlement.getStatus());
        response.setProcessedAt(settlement.getProcessedAt());
        response.setCreatedAt(settlement.getCreatedAt());
        response.setUpdatedAt(settlement.getUpdatedAt());
        return response;
    }

    // Helper: Map Payout to Response
    private PayoutResponse mapToPayoutResponse(Payout payout) {
        PayoutResponse response = new PayoutResponse();
        response.setId(payout.getId());
        response.setSettlementId(payout.getSettlementId());
        response.setAmount(payout.getAmount());
        response.setPaymentMethod(payout.getPaymentMethod());
        response.setAccountDetails(payout.getAccountDetails());
        response.setTransactionId(payout.getTransactionId());
        response.setStatus(payout.getStatus());
        response.setInitiatedAt(payout.getInitiatedAt());
        response.setCompletedAt(payout.getCompletedAt());
        response.setCreatedAt(payout.getCreatedAt());
        return response;
    }
}
