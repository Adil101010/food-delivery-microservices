package com.fooddelivery.promoservice.repository;

import com.fooddelivery.promoservice.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

    List<CouponUsage> findByCouponId(Long couponId);

    List<CouponUsage> findByUserId(Long userId);

    List<CouponUsage> findByCouponIdAndUserId(Long couponId, Long userId);

    int countByCouponIdAndUserId(Long couponId, Long userId);

    boolean existsByOrderId(Long orderId);
}
