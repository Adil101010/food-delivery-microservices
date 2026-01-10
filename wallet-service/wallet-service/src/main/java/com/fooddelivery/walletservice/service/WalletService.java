package com.fooddelivery.walletservice.service;

import com.fooddelivery.walletservice.dto.*;
import com.fooddelivery.walletservice.entity.Wallet;
import com.fooddelivery.walletservice.entity.WalletTransaction;
import com.fooddelivery.walletservice.enums.TransactionType;
import com.fooddelivery.walletservice.enums.WalletStatus;
import com.fooddelivery.walletservice.enums.WalletTransactionType;
import com.fooddelivery.walletservice.repository.WalletRepository;
import com.fooddelivery.walletservice.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    // Get or Create Wallet
    public WalletResponse getOrCreateWallet(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wallet newWallet = new Wallet();
                    newWallet.setUserId(userId);
                    newWallet.setBalance(BigDecimal.ZERO);
                    newWallet.setStatus(WalletStatus.ACTIVE);
                    return walletRepository.save(newWallet);
                });
        return mapToWalletResponse(wallet);
    }

    // Get Wallet Balance
    public BalanceResponse getBalance(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + userId));
        return new BalanceResponse(userId, wallet.getBalance());
    }

    // Add Money to Wallet
    @Transactional
    public WalletResponse addMoney(AddMoneyRequest request) {
        Wallet wallet = walletRepository.findByUserId(request.getUserId())
                .orElseGet(() -> {
                    Wallet newWallet = new Wallet();
                    newWallet.setUserId(request.getUserId());
                    newWallet.setBalance(BigDecimal.ZERO);
                    newWallet.setStatus(WalletStatus.ACTIVE);
                    return walletRepository.save(newWallet);
                });

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new RuntimeException("Wallet is not active. Status: " + wallet.getStatus());
        }

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(request.getAmount());
        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);

        // Create transaction record
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(wallet.getId());
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.CREDIT);
        transaction.setTransactionType(WalletTransactionType.ADD_MONEY);
        transaction.setDescription(request.getDescription() != null ? request.getDescription() : "Money added to wallet");
        transaction.setReferenceId(request.getReferenceId());
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transactionRepository.save(transaction);

        return mapToWalletResponse(wallet);
    }

    // Deduct Money from Wallet
    @Transactional
    public WalletResponse deductMoney(DeductMoneyRequest request) {
        Wallet wallet = walletRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + request.getUserId()));

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new RuntimeException("Wallet is not active. Status: " + wallet.getStatus());
        }

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance. Available: " + wallet.getBalance());
        }

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(request.getAmount());
        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);

        // Create transaction record
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(wallet.getId());
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.DEBIT);
        transaction.setTransactionType(WalletTransactionType.ORDER_PAYMENT);
        transaction.setDescription(request.getDescription() != null ? request.getDescription() : "Payment for order");
        transaction.setOrderId(request.getOrderId());
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transactionRepository.save(transaction);

        return mapToWalletResponse(wallet);
    }

    // Add Cashback
    @Transactional
    public WalletResponse addCashback(CashbackRequest request) {
        Wallet wallet = walletRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + request.getUserId()));

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new RuntimeException("Wallet is not active. Status: " + wallet.getStatus());
        }

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(request.getAmount());
        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);

        // Create transaction record
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(wallet.getId());
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.CREDIT);
        transaction.setTransactionType(WalletTransactionType.CASHBACK);
        transaction.setDescription(request.getDescription() != null ? request.getDescription() : "Cashback credited");
        transaction.setOrderId(request.getOrderId());
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transactionRepository.save(transaction);

        return mapToWalletResponse(wallet);
    }

    // Process Refund
    @Transactional
    public WalletResponse processRefund(RefundRequest request) {
        Wallet wallet = walletRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + request.getUserId()));

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(request.getAmount());
        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);

        // Create transaction record
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(wallet.getId());
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.CREDIT);
        transaction.setTransactionType(WalletTransactionType.REFUND);
        transaction.setDescription(request.getDescription() != null ? request.getDescription() : "Refund for order #" + request.getOrderId());
        transaction.setOrderId(request.getOrderId());
        transaction.setReferenceId(request.getReferenceId());
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transactionRepository.save(transaction);

        return mapToWalletResponse(wallet);
    }

    // Get Transaction History
    public List<WalletTransactionResponse> getTransactionHistory(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + userId));

        List<WalletTransaction> transactions = transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId());
        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    // Get Transactions by Type
    public List<WalletTransactionResponse> getTransactionsByType(Long userId, TransactionType type) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + userId));

        List<WalletTransaction> transactions = transactionRepository.findByWalletIdAndTypeOrderByCreatedAtDesc(wallet.getId(), type);
        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    // Helper: Map Wallet to Response
    private WalletResponse mapToWalletResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setId(wallet.getId());
        response.setUserId(wallet.getUserId());
        response.setBalance(wallet.getBalance());
        response.setStatus(wallet.getStatus());
        response.setCreatedAt(wallet.getCreatedAt());
        response.setUpdatedAt(wallet.getUpdatedAt());
        return response;
    }

    // Helper: Map Transaction to Response
    private WalletTransactionResponse mapToTransactionResponse(WalletTransaction transaction) {
        WalletTransactionResponse response = new WalletTransactionResponse();
        response.setId(transaction.getId());
        response.setWalletId(transaction.getWalletId());
        response.setAmount(transaction.getAmount());
        response.setType(transaction.getType());
        response.setTransactionType(transaction.getTransactionType());
        response.setDescription(transaction.getDescription());
        response.setOrderId(transaction.getOrderId());
        response.setReferenceId(transaction.getReferenceId());
        response.setBalanceBefore(transaction.getBalanceBefore());
        response.setBalanceAfter(transaction.getBalanceAfter());
        response.setCreatedAt(transaction.getCreatedAt());
        return response;
    }
}
