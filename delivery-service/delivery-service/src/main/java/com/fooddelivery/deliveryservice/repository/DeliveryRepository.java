package com.fooddelivery.deliveryservice.repository;

import com.fooddelivery.deliveryservice.entity.Delivery;
import com.fooddelivery.deliveryservice.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByOrderId(Long orderId);

    List<Delivery> findByPartnerId(Long partnerId);

    List<Delivery> findByCustomerId(Long customerId);

    List<Delivery> findByStatus(DeliveryStatus status);

    List<Delivery> findByPartnerIdAndStatus(Long partnerId, DeliveryStatus status);
}
