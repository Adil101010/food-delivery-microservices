package com.fooddelivery.settlementservice.repository;

import com.fooddelivery.settlementservice.entity.Settlement;
import com.fooddelivery.settlementservice.enums.EntityType;
import com.fooddelivery.settlementservice.enums.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    List<Settlement> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);

    List<Settlement> findByStatus(SettlementStatus status);

    Optional<Settlement> findByEntityTypeAndEntityIdAndPeriodStartAndPeriodEnd(
            EntityType entityType, Long entityId, LocalDate periodStart, LocalDate periodEnd);

    @Query("SELECT s FROM Settlement s WHERE s.entityType = :entityType AND s.entityId = :entityId AND s.status = :status")
    List<Settlement> findByEntityAndStatus(EntityType entityType, Long entityId, SettlementStatus status);

    List<Settlement> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(EntityType entityType, Long entityId);
}
