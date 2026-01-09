package com.fooddelivery.reviewservice.repository;

import com.fooddelivery.reviewservice.entity.DeliveryRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRatingRepository extends JpaRepository<DeliveryRating, Long> {

    Optional<DeliveryRating> findByOrderId(Long orderId);

    List<DeliveryRating> findByDeliveryPartnerId(Long deliveryPartnerId);

    List<DeliveryRating> findByUserId(Long userId);

    @Query("SELECT AVG(dr.rating) FROM DeliveryRating dr WHERE dr.deliveryPartnerId = :partnerId")
    Double getAveragePartnerRating(@Param("partnerId") Long partnerId);

    @Query("SELECT COUNT(dr) FROM DeliveryRating dr WHERE dr.deliveryPartnerId = :partnerId")
    Long getPartnerRatingCount(@Param("partnerId") Long partnerId);
}
