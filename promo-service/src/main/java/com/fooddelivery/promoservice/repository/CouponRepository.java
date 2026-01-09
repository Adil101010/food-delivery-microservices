package com.fooddelivery.promoservice.repository;

import com.fooddelivery.promoservice.entity.Coupon;
import com.fooddelivery.promoservice.enums.CouponStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    List<Coupon> findByStatus(CouponStatus status);

    List<Coupon> findByRestaurantId(Long restaurantId);

    boolean existsByCode(String code);
}
