package com.fooddelivery.authservice.repository;

import com.fooddelivery.authservice.entity.Otp;
import com.fooddelivery.authservice.enums.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findByEmailAndOtpTypeAndIsUsed(String email, OtpType otpType, Boolean isUsed);

    Optional<Otp> findByPhoneAndOtpTypeAndIsUsed(String phone, OtpType otpType, Boolean isUsed);

    Optional<Otp> findByUserIdAndOtpTypeAndIsUsed(Long userId, OtpType otpType, Boolean isUsed);

    List<Otp> findByUserId(Long userId);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);

    void deleteByUserId(Long userId);
}
