package com.fooddelivery.locationservice.repository;

import com.fooddelivery.locationservice.entity.LocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistory, Long> {

    List<LocationHistory> findByPartnerId(Long partnerId);

    List<LocationHistory> findByDeliveryId(Long deliveryId);

    List<LocationHistory> findByPartnerIdAndTimestampBetween(
            Long partnerId, LocalDateTime startTime, LocalDateTime endTime);
}
