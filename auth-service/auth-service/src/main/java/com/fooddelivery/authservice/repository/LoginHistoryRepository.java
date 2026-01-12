package com.fooddelivery.authservice.repository;

import com.fooddelivery.authservice.entity.LoginHistory;
import com.fooddelivery.authservice.enums.LoginStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    List<LoginHistory> findByUserId(Long userId);

    List<LoginHistory> findByUserIdAndLoginStatus(Long userId, LoginStatus loginStatus);

    List<LoginHistory> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    List<LoginHistory> findByIpAddress(String ipAddress);

    long countByUserIdAndLoginStatusAndCreatedAtAfter(Long userId, LoginStatus loginStatus, LocalDateTime after);
}
