package com.fooddelivery.paymentservice.service;

import com.fooddelivery.paymentservice.dto.*;
import com.fooddelivery.paymentservice.entity.Payment;
import com.fooddelivery.paymentservice.entity.Transaction;
import com.fooddelivery.paymentservice.enums.PaymentMethod;
import com.fooddelivery.paymentservice.enums.PaymentStatus;
import com.fooddelivery.paymentservice.enums.TransactionType;
import com.fooddelivery.paymentservice.repository.PaymentRepository;
import com.fooddelivery.paymentservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;

    // Create Payment
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {

        // Check if payment already exists for order
        if (paymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new RuntimeException("Payment already exists for this order");
        }

        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setUserId(request.getUserId());
        payment.setAmount(request.getAmount());
        payment.setCurrency("INR");
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentDescription(request.getPaymentDescription());
        payment.setStatus(PaymentStatus.PENDING);

        // Generate transaction ID
        String transactionId = "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        payment.setTransactionId(transactionId);

        // Generate gateway order ID (mock)
        String gatewayOrderId = "ORDER_" + System.currentTimeMillis();
        payment.setGatewayOrderId(gatewayOrderId);

        Payment savedPayment = paymentRepository.save(payment);

        // Create transaction log
        createTransactionLog(savedPayment, TransactionType.PAYMENT, PaymentStatus.PENDING, "Payment created");

        log.info("Payment created: {} for order: {}", transactionId, request.getOrderId());

        return convertToResponse(savedPayment);
    }

    // Process Payment (Mock gateway processing)
    @Transactional
    public PaymentResponse processPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Payment cannot be processed in current status");
        }

        payment.setStatus(PaymentStatus.PROCESSING);
        paymentRepository.save(payment);

        // Mock processing delay
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Mock payment success/failure (90% success rate)
        boolean isSuccess = Math.random() < 0.9;

        if (isSuccess) {
            // Payment Success
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setGatewayPaymentId("PAY_" + System.currentTimeMillis());
            payment.setGatewaySignature("SIG_" + UUID.randomUUID().toString().substring(0, 16));
            payment.setPaidAt(LocalDateTime.now());

            createTransactionLog(payment, TransactionType.PAYMENT, PaymentStatus.SUCCESS, "Payment successful");

            log.info("Payment successful: {}", payment.getTransactionId());
        } else {
            // Payment Failed
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment declined by bank");

            createTransactionLog(payment, TransactionType.PAYMENT, PaymentStatus.FAILED, "Payment failed");

            log.warn("Payment failed: {}", payment.getTransactionId());
        }

        Payment updatedPayment = paymentRepository.save(payment);
        return convertToResponse(updatedPayment);
    }

    // Verify Payment (For gateway callback)
    @Transactional
    public PaymentResponse verifyPayment(VerifyPaymentRequest request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // Mock signature verification
        boolean isValid = request.getGatewaySignature() != null &&
                request.getGatewaySignature().startsWith("SIG_");

        if (isValid) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setGatewayPaymentId(request.getGatewayPaymentId());
            payment.setGatewaySignature(request.getGatewaySignature());
            payment.setPaidAt(LocalDateTime.now());

            createTransactionLog(payment, TransactionType.PAYMENT, PaymentStatus.SUCCESS, "Payment verified");
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Invalid signature");

            createTransactionLog(payment, TransactionType.PAYMENT, PaymentStatus.FAILED, "Verification failed");
        }

        Payment verifiedPayment = paymentRepository.save(payment);
        return convertToResponse(verifiedPayment);
    }

    // Process Refund
    @Transactional
    public PaymentResponse processRefund(RefundRequest request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new RuntimeException("Cannot refund payment that is not successful");
        }

        Double refundAmount = request.getRefundAmount() != null ?
                request.getRefundAmount() : payment.getAmount();

        if (refundAmount > payment.getAmount()) {
            throw new RuntimeException("Refund amount cannot be greater than payment amount");
        }

        payment.setStatus(PaymentStatus.REFUND_INITIATED);
        payment.setRefundAmount(refundAmount);

        paymentRepository.save(payment);

        // Mock refund processing
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Refund success
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundTransactionId("REFUND_" + System.currentTimeMillis());
        payment.setRefundedAt(LocalDateTime.now());

        createTransactionLog(payment, TransactionType.REFUND, PaymentStatus.REFUNDED,
                "Refund processed: ₹" + refundAmount);

        Payment refundedPayment = paymentRepository.save(payment);

        log.info("Refund processed: {} for payment: {}", refundAmount, payment.getTransactionId());

        return convertToResponse(refundedPayment);
    }

    // Get Payment by ID
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return convertToResponse(payment);
    }

    // Get Payment by Order ID
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order"));
        return convertToResponse(payment);
    }

    // Get User Payments
    public List<PaymentResponse> getUserPayments(Long userId) {
        List<Payment> payments = paymentRepository.findByUserId(userId);
        return payments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get Payment Transactions
    public List<TransactionResponse> getPaymentTransactions(Long paymentId) {
        List<Transaction> transactions = transactionRepository.findByPaymentId(paymentId);
        return transactions.stream()
                .map(this::convertToTransactionResponse)
                .collect(Collectors.toList());
    }

    // Create Transaction Log
    private void createTransactionLog(Payment payment, TransactionType type,
                                      PaymentStatus status, String message) {
        Transaction transaction = new Transaction();
        transaction.setPaymentId(payment.getId());
        transaction.setOrderId(payment.getOrderId());
        transaction.setType(type);
        transaction.setStatus(status);
        transaction.setAmount(type == TransactionType.REFUND ? payment.getRefundAmount() : payment.getAmount());
        transaction.setTransactionId(payment.getTransactionId() + "_" + System.currentTimeMillis());
        transaction.setMessage(message);
        transaction.setTimestamp(LocalDateTime.now());

        transactionRepository.save(transaction);
    }

    // Convert Payment to Response
    private PaymentResponse convertToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrderId());
        response.setUserId(payment.getUserId());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setStatus(payment.getStatus());
        response.setTransactionId(payment.getTransactionId());
        response.setGatewayOrderId(payment.getGatewayOrderId());
        response.setGatewayPaymentId(payment.getGatewayPaymentId());
        response.setPaymentDescription(payment.getPaymentDescription());
        response.setFailureReason(payment.getFailureReason());
        response.setRefundAmount(payment.getRefundAmount());
        response.setRefundTransactionId(payment.getRefundTransactionId());
        response.setPaidAt(payment.getPaidAt());
        response.setRefundedAt(payment.getRefundedAt());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        return response;
    }

    // Convert Transaction to Response
    private TransactionResponse convertToTransactionResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setPaymentId(transaction.getPaymentId());
        response.setOrderId(transaction.getOrderId());
        response.setType(transaction.getType());
        response.setStatus(transaction.getStatus());
        response.setAmount(transaction.getAmount());
        response.setTransactionId(transaction.getTransactionId());
        response.setMessage(transaction.getMessage());
        response.setTimestamp(transaction.getTimestamp());
        return response;
    }
}
