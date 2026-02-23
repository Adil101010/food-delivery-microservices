package com.fooddelivery.paymentservice.service;

import com.fooddelivery.paymentservice.dto.*;
import com.fooddelivery.paymentservice.entity.Payment;
import com.fooddelivery.paymentservice.entity.Transaction;
import com.fooddelivery.paymentservice.enums.PaymentMethod;
import com.fooddelivery.paymentservice.enums.PaymentStatus;
import com.fooddelivery.paymentservice.enums.TransactionType;
import com.fooddelivery.paymentservice.exception.BadRequestException;
import com.fooddelivery.paymentservice.exception.PaymentException;
import com.fooddelivery.paymentservice.exception.ResourceNotFoundException;
import com.fooddelivery.paymentservice.repository.PaymentRepository;
import com.fooddelivery.paymentservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        log.info("Creating payment for orderId: {}, userId: {}, amount: {}",
                request.getOrderId(), request.getUserId(), request.getAmount());

        try {
            validateCreatePaymentRequest(request);

            paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(request.getOrderId())
                    .ifPresent(existingPayment -> {
                        if (existingPayment.getStatus() == PaymentStatus.PENDING ||
                                existingPayment.getStatus() == PaymentStatus.SUCCESS) {
                            log.warn("Payment already exists for orderId: {}", request.getOrderId());
                            throw new BadRequestException(
                                    "Payment already exists for order: " + request.getOrderId());
                        }
                    });

            Payment payment = Payment.builder()
                    .orderId(request.getOrderId())
                    .userId(request.getUserId())
                    .amount(request.getAmount())
                    .currency("INR")
                    .paymentMethod(request.getPaymentMethod())
                    .status(PaymentStatus.PENDING)
                    .paymentDescription(request.getPaymentDescription())
                    .customerName(request.getCustomerName())
                    .customerEmail(request.getCustomerEmail())
                    .customerPhone(request.getCustomerPhone())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Payment savedPayment = paymentRepository.save(payment);
            log.info("Payment created successfully with id: {}", savedPayment.getId());

            Transaction transaction = Transaction.builder()
                    .payment(savedPayment)
                    .type(TransactionType.PAYMENT)
                    .amount(request.getAmount())
                    .status(PaymentStatus.PENDING)
                    .description("Payment created")
                    .createdAt(LocalDateTime.now())
                    .build();

            transactionRepository.save(transaction);

            return mapToPaymentResponse(savedPayment);

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating payment: {}", e.getMessage(), e);
            throw new PaymentException("Failed to create payment", e);
        }
    }

    @Transactional
    public PaymentResponse processPayment(Long paymentId) {
        log.info("Processing payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Payment cannot be processed. Current status: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setUpdatedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        Transaction transaction = Transaction.builder()
                .payment(payment)
                .type(TransactionType.PAYMENT)
                .amount(payment.getAmount())
                .status(PaymentStatus.PROCESSING)
                .description("Payment processing started")
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        log.info("Payment processing started for id: {}", paymentId);
        return mapToPaymentResponse(payment);
    }

    @Transactional
    public PaymentResponse verifyPayment(VerifyPaymentRequest request) {
        log.info("Verifying payment: {}", request.getPaymentId());

        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", request.getPaymentId()));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.warn("Payment already verified: {}", request.getPaymentId());
            return mapToPaymentResponse(payment);
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setGatewayPaymentId(request.getGatewayPaymentId());
        payment.setGatewaySignature(request.getGatewaySignature());
        payment.setGatewayOrderId(request.getGatewayOrderId());
        payment.setPaidAt(LocalDateTime.now());
        payment.setCompletedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        Transaction transaction = Transaction.builder()
                .payment(payment)
                .type(TransactionType.PAYMENT)
                .amount(payment.getAmount())
                .status(PaymentStatus.SUCCESS)
                .gatewayTransactionId(request.getGatewayPaymentId())
                .description("Payment verified successfully")
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        notifyOrderService(payment);

        log.info("Payment verified successfully: {}", request.getPaymentId());
        return mapToPaymentResponse(payment);
    }

    @Transactional
    public PaymentResponse processRefund(RefundRequest request) {
        log.info("Processing refund for payment: {}, amount: {}",
                request.getPaymentId(), request.getRefundAmount());

        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", request.getPaymentId()));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new BadRequestException("Only successful payments can be refunded");
        }

        if (request.getRefundAmount().compareTo(payment.getAmount()) > 0) {
            throw new BadRequestException("Refund amount cannot exceed payment amount");
        }

        if (payment.getRefundAmount() != null &&
                payment.getRefundAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BadRequestException("Payment already refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundAmount(request.getRefundAmount());
        payment.setRefundTransactionId("REFUND_" + System.currentTimeMillis());
        payment.setRefundedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        Transaction transaction = Transaction.builder()
                .payment(payment)
                .type(TransactionType.REFUND)
                .amount(request.getRefundAmount())
                .status(PaymentStatus.REFUNDED)
                .description("Refund: " + request.getRefundReason())
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        log.info("Refund processed successfully for payment: {}", request.getPaymentId());
        return mapToPaymentResponse(payment);
    }

    // ---- COD ----

    // Method signature update karo
    @Transactional
    public PaymentResponse processCOD(Long orderId, Long userId) {
        log.info("Processing COD payment for orderId: {}, userId: {}", orderId, userId);

        try {
            Optional<Payment> existingOpt =
                    paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(orderId);

            if (existingOpt.isPresent()) {
                Payment existing = existingOpt.get();
                if (existing.getStatus() == PaymentStatus.SUCCESS) {
                    throw new BadRequestException(
                            "Payment already completed for order: " + orderId);
                }
                log.info("Deleting existing {} payment for orderId: {}",
                        existing.getStatus(), orderId);
                transactionRepository.deleteByPayment(existing);
                paymentRepository.delete(existing);
                paymentRepository.flush();
            }

            Payment payment = Payment.builder()
                    .orderId(orderId)
                    .userId(userId)        // userId add kiya
                    .amount(BigDecimal.ZERO)
                    .currency("INR")
                    .paymentMethod(PaymentMethod.CASH_ON_DELIVERY)
                    .status(PaymentStatus.PENDING)
                    .paymentDescription("Cash on Delivery")
                    .transactionId("COD_" + orderId + "_" + System.currentTimeMillis())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Payment savedPayment = paymentRepository.save(payment);

            Transaction transaction = Transaction.builder()
                    .payment(savedPayment)
                    .type(TransactionType.PAYMENT)
                    .amount(BigDecimal.ZERO)
                    .status(PaymentStatus.PENDING)
                    .description("COD order placed - payment on delivery")
                    .createdAt(LocalDateTime.now())
                    .build();

            transactionRepository.save(transaction);
            notifyOrderServiceCOD(savedPayment);

            log.info("COD payment created successfully for orderId: {}", orderId);
            return mapToPaymentResponse(savedPayment);

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("COD processing failed for orderId: {}", orderId, e);
            throw new PaymentException("COD processing failed: " + e.getMessage(), e);
        }
    }

    private void notifyOrderServiceCOD(Payment payment) {
        try {
            String orderServiceUrl = "http://localhost:8088/api/orders/webhook/payment-status";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("orderId", payment.getOrderId());
            requestBody.put("paymentStatus", "CONFIRMED");
            requestBody.put("paymentMethod", "CASH_ON_DELIVERY");
            requestBody.put("transactionId", payment.getTransactionId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    orderServiceUrl, entity, String.class);

            log.info("Order Service notified for COD order: {}. Status: {}",
                    payment.getOrderId(), response.getStatusCode());

        } catch (Exception e) {
            log.error("Failed to notify Order Service for COD order: {}. Error: {}",
                    payment.getOrderId(), e.getMessage());
        }
    }

    // ---- Getters ----

    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        return mapToPaymentResponse(payment);
    }

    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        return mapToPaymentResponse(payment);
    }

    public List<PaymentResponse> getUserPayments(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> getPaymentTransactions(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        return transactionRepository.findByPaymentOrderByCreatedAtDesc(payment)
                .stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    // ---- Private helpers ----

    private void validateCreatePaymentRequest(CreatePaymentRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }
        if (request.getAmount().compareTo(new BigDecimal("100000")) > 0) {
            throw new BadRequestException("Amount cannot exceed 1,00,000");
        }
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .paymentDescription(payment.getPaymentDescription())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .razorpaySignature(payment.getRazorpaySignature())
                .gatewayOrderId(payment.getGatewayOrderId())
                .gatewayPaymentId(payment.getGatewayPaymentId())
                .gatewaySignature(payment.getGatewaySignature())
                .customerName(payment.getCustomerName())
                .customerEmail(payment.getCustomerEmail())
                .customerPhone(payment.getCustomerPhone())
                .transactionId(payment.getTransactionId())
                .failureReason(payment.getFailureReason())
                .refundAmount(payment.getRefundAmount())
                .refundTransactionId(payment.getRefundTransactionId())
                .refundedAt(payment.getRefundedAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .paidAt(payment.getPaidAt())
                .completedAt(payment.getCompletedAt())
                .message("Payment retrieved successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .paymentId(transaction.getPayment().getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .gatewayTransactionId(transaction.getGatewayTransactionId())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .transactionId(transaction.getGatewayTransactionId())
                .message("Transaction retrieved successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    private void notifyOrderService(Payment payment) {
        try {
            String orderServiceUrl = "http://localhost:8088/api/orders/webhook/payment-status";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("orderId", payment.getOrderId());
            requestBody.put("paymentStatus", payment.getStatus().name());
            requestBody.put("razorpayPaymentId", payment.getRazorpayPaymentId());
            requestBody.put("razorpayOrderId", payment.getRazorpayOrderId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    orderServiceUrl, entity, String.class);

            log.info("Order Service notified successfully. Response: {}", response.getStatusCode());

        } catch (Exception e) {
            log.error("Failed to notify Order Service for order: {}. Error: {}",
                    payment.getOrderId(), e.getMessage(), e);
        }
    }
}
