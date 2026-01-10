package com.fooddelivery.walletservice.repository;

import com.fooddelivery.walletservice.entity.WalletTransaction;
import com.fooddelivery.walletservice.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);

    List<WalletTransaction> findByWalletIdAndTypeOrderByCreatedAtDesc(Long walletId, TransactionType type);

    @Query("SELECT t FROM WalletTransaction t WHERE t.orderId = :orderId")
    List<WalletTransaction> findByOrderId(Long orderId);
}
