package com.fooddelivery.authservice.repository;

import com.fooddelivery.authservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    void deleteByToken(String token);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);

    List<RefreshToken> findByUserIdAndIsRevoked(Long userId, Boolean isRevoked);
}
