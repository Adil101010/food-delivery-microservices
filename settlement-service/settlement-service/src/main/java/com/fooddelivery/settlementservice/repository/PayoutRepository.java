package com.fooddelivery.settlementservice.repository;

import com.fooddelivery.settlementservice.entity.Payout;
import com.fooddelivery.settlementservice.enums.PayoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, Long> {

    Optional<Payout> findBySettlementId(Long settlementId);

    List<Payout> findByStatus(PayoutStatus status);

    List<Payout> findBySettlementIdOrderByCreatedAtDesc(Long settlementId);
}
