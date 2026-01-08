package com.fooddelivery.locationservice.repository;

import com.fooddelivery.locationservice.entity.PartnerLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerLocationRepository extends JpaRepository<PartnerLocation, Long> {

    Optional<PartnerLocation> findByPartnerId(Long partnerId);

    List<PartnerLocation> findByIsOnline(Boolean isOnline);

    List<PartnerLocation> findByIsOnlineAndIsMoving(Boolean isOnline, Boolean isMoving);

    @Query("SELECT pl FROM PartnerLocation pl WHERE pl.isOnline = true " +
            "AND pl.latitude BETWEEN :minLat AND :maxLat " +
            "AND pl.longitude BETWEEN :minLon AND :maxLon")
    List<PartnerLocation> findPartnersInArea(
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLon") Double minLon,
            @Param("maxLon") Double maxLon
    );
}
