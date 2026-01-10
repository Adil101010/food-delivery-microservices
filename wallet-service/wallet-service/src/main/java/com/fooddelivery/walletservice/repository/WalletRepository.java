package com.fooddelivery.walletservice.repository;

import com.fooddelivery.walletservice.entity.Wallet;
import com.fooddelivery.walletservice.enums.WalletStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    Optional<Wallet> findByUserIdAndStatus(Long userId, WalletStatus status);
}
