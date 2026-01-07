package com.fooddelivery.deliveryservice.repository;

import com.fooddelivery.deliveryservice.entity.DeliveryPartner;
import com.fooddelivery.deliveryservice.enums.AvailabilityStatus;
import com.fooddelivery.deliveryservice.enums.PartnerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {

    Optional<DeliveryPartner> findByUserId(Long userId);

    Optional<DeliveryPartner> findByEmail(String email);

    Optional<DeliveryPartner> findByPhone(String phone);

    List<DeliveryPartner> findByStatus(PartnerStatus status);

    List<DeliveryPartner> findByAvailability(AvailabilityStatus availability);

    List<DeliveryPartner> findByCityAndAvailability(String city, AvailabilityStatus availability);
}
