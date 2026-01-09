package com.fooddelivery.paymentservice.controller;

import com.fooddelivery.paymentservice.dto.*;
import com.fooddelivery.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    // Health Check
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        return ResponseEntity.ok(new MessageResponse("Payment Service is running"));
    }

    // Create Payment
    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse payment = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    // Process Payment (Mock gateway processing)
    @PostMapping("/{paymentId}/process")
    public ResponseEntity<PaymentResponse> processPayment(@PathVariable Long paymentId) {
        PaymentResponse payment = paymentService.processPayment(paymentId);
        return ResponseEntity.ok(payment);
    }

    // Verify Payment (Gateway callback)
    @PostMapping("/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request) {
        PaymentResponse payment = paymentService.verifyPayment(request);
        return ResponseEntity.ok(payment);
    }

    // Process Refund
    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> processRefund(
            @Valid @RequestBody RefundRequest request) {
        PaymentResponse payment = paymentService.processRefund(request);
        return ResponseEntity.ok(payment);
    }

    // Get Payment by ID
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        PaymentResponse payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    // Get Payment by Order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentResponse payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(payment);
    }

    // Get User Payments
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getUserPayments(@PathVariable Long userId) {
        List<PaymentResponse> payments = paymentService.getUserPayments(userId);
        return ResponseEntity.ok(payments);
    }

    // Get Payment Transactions (History)
    @GetMapping("/{paymentId}/transactions")
    public ResponseEntity<List<TransactionResponse>> getPaymentTransactions(@PathVariable Long paymentId) {
        List<TransactionResponse> transactions = paymentService.getPaymentTransactions(paymentId);
        return ResponseEntity.ok(transactions);
    }
}
