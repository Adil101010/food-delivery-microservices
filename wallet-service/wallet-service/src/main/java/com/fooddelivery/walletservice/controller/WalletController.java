package com.fooddelivery.walletservice.controller;

import com.fooddelivery.walletservice.dto.*;
import com.fooddelivery.walletservice.enums.TransactionType;
import com.fooddelivery.walletservice.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WalletController {

    private final WalletService walletService;

    // Health Check
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        return ResponseEntity.ok(new MessageResponse("Wallet Service is running"));
    }

    // Get or Create Wallet
    @GetMapping("/user/{userId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable Long userId) {
        WalletResponse wallet = walletService.getOrCreateWallet(userId);
        return ResponseEntity.ok(wallet);
    }

    // Get Wallet Balance
    @GetMapping("/user/{userId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable Long userId) {
        BalanceResponse balance = walletService.getBalance(userId);
        return ResponseEntity.ok(balance);
    }

    // Add Money to Wallet
    @PostMapping("/add-money")
    public ResponseEntity<WalletResponse> addMoney(@Valid @RequestBody AddMoneyRequest request) {
        WalletResponse wallet = walletService.addMoney(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
    }

    // Deduct Money from Wallet
    @PostMapping("/deduct")
    public ResponseEntity<WalletResponse> deductMoney(@Valid @RequestBody DeductMoneyRequest request) {
        WalletResponse wallet = walletService.deductMoney(request);
        return ResponseEntity.ok(wallet);
    }

    // Add Cashback
    @PostMapping("/cashback")
    public ResponseEntity<WalletResponse> addCashback(@Valid @RequestBody CashbackRequest request) {
        WalletResponse wallet = walletService.addCashback(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
    }

    // Process Refund
    @PostMapping("/refund")
    public ResponseEntity<WalletResponse> processRefund(@Valid @RequestBody RefundRequest request) {
        WalletResponse wallet = walletService.processRefund(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
    }

    // Get Transaction History
    @GetMapping("/user/{userId}/transactions")
    public ResponseEntity<List<WalletTransactionResponse>> getTransactionHistory(@PathVariable Long userId) {
        List<WalletTransactionResponse> transactions = walletService.getTransactionHistory(userId);
        return ResponseEntity.ok(transactions);
    }

    // Get Transactions by Type (CREDIT or DEBIT)
    @GetMapping("/user/{userId}/transactions/{type}")
    public ResponseEntity<List<WalletTransactionResponse>> getTransactionsByType(
            @PathVariable Long userId,
            @PathVariable TransactionType type) {
        List<WalletTransactionResponse> transactions = walletService.getTransactionsByType(userId, type);
        return ResponseEntity.ok(transactions);
    }
}
