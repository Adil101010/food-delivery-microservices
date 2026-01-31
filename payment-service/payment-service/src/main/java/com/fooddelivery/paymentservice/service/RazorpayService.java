package com.fooddelivery.paymentservice.service;

import com.fooddelivery.paymentservice.dto.*;
import com.fooddelivery.paymentservice.entity.Payment;
import com.fooddelivery.paymentservice.entity.Transaction;
import com.fooddelivery.paymentservice.enums.PaymentMethod;
import com.fooddelivery.paymentservice.enums.PaymentStatus;
import com.fooddelivery.paymentservice.enums.TransactionType;
import com.fooddelivery.paymentservice.exception.BadRequestException;
import com.fooddelivery.paymentservice.exception.PaymentException;
import com.fooddelivery.paymentservice.exception.RazorpayException;
import com.fooddelivery.paymentservice.exception.ResourceNotFoundException;
import com.fooddelivery.paymentservice.repository.PaymentRepository;
import com.fooddelivery.paymentservice.repository.TransactionRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RazorpayService {

    private final RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.currency:INR}")
    private String currency;

    @Value("${razorpay.company.name:Food Delivery Platform}")
    private String companyName;

    @Value("${payment.callback.url:http://localhost:8089/api/payments/callback}")
    private String callbackUrl;

    @Value("${payment.logo.url:https://your-logo-url.com/logo.png}")
    private String logoUrl;

    @Value("${razorpay.test.mode:false}")
    private boolean testMode;

    @Value("${order.service.url:http://localhost:8084}")
    private String orderServiceUrl;

    @Transactional
    public PaymentOrderResponse createOrder(PaymentOrderRequest request) {
        log.info("Creating Razorpay order for orderId: {}, userId: {}, amount: {}",
                request.getOrderId(), request.getUserId(), request.getAmount());

        try {
            validateOrderRequest(request);

            paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(request.getOrderId())
                    .ifPresent(existingPayment -> {
                        if (existingPayment.getStatus() == PaymentStatus.PENDING ||
                                existingPayment.getStatus() == PaymentStatus.SUCCESS) {
                            log.warn("Payment already exists for orderId: {} with status: {}",
                                    request.getOrderId(), existingPayment.getStatus());
                            throw new BadRequestException(
                                    "Payment already exists for order: " + request.getOrderId());
                        }
                    });

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", convertToRazorpayAmount(request.getAmount()));
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", "ORDER_" + request.getOrderId() + "_" + System.currentTimeMillis());

            Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");

            log.info("Razorpay order created successfully: {}", razorpayOrderId);

            Payment payment = Payment.builder()
                    .orderId(request.getOrderId())
                    .userId(request.getUserId())
                    .amount(request.getAmount())
                    .currency(currency)
                    .paymentMethod(PaymentMethod.RAZORPAY)
                    .status(PaymentStatus.PENDING)
                    .razorpayOrderId(razorpayOrderId)
                    .customerName(request.getCustomerName())
                    .customerEmail(request.getCustomerEmail())
                    .customerPhone(request.getCustomerPhone())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Payment savedPayment = paymentRepository.save(payment);
            log.info("Payment record saved in database with id: {}", savedPayment.getId());

            Transaction transaction = Transaction.builder()
                    .payment(savedPayment)
                    .type(TransactionType.PAYMENT)
                    .amount(request.getAmount())
                    .status(PaymentStatus.PENDING)
                    .gatewayTransactionId(razorpayOrderId)
                    .createdAt(LocalDateTime.now())
                    .build();

            transactionRepository.save(transaction);
            log.info("Transaction record created for payment id: {}", savedPayment.getId());

            PaymentOrderResponse.PaymentOptions.PrefillData prefillData =
                    PaymentOrderResponse.PaymentOptions.PrefillData.builder()
                            .name(request.getCustomerName())
                            .email(request.getCustomerEmail())
                            .contact(request.getCustomerPhone())
                            .build();

            PaymentOrderResponse.PaymentOptions paymentOptions =
                    PaymentOrderResponse.PaymentOptions.builder()
                            .key(keyId)
                            .orderId(razorpayOrderId)
                            .amount(convertToRazorpayAmount(request.getAmount()))
                            .currency(currency)
                            .name(companyName)
                            .description("Food Order #" + request.getOrderId())
                            .image(logoUrl)
                            .prefill(prefillData)
                            .callbackUrl(callbackUrl)
                            .build();

            PaymentOrderResponse response = PaymentOrderResponse.builder()
                    .razorpayOrderId(razorpayOrderId)
                    .orderId(request.getOrderId())
                    .amount(request.getAmount())
                    .currency(currency)
                    .status(PaymentStatus.PENDING.name())
                    .keyId(keyId)
                    .customerName(request.getCustomerName())
                    .customerEmail(request.getCustomerEmail())
                    .customerPhone(request.getCustomerPhone())
                    .paymentOptions(paymentOptions)
                    .createdAt(LocalDateTime.now())
                    .build();

            log.info("Payment order response created successfully for orderId: {}", request.getOrderId());
            return response;

        } catch (BadRequestException e) {
            log.error("Bad request while creating order: {}", e.getMessage());
            throw e;

        } catch (com.razorpay.RazorpayException e) {
            log.error("Razorpay API error while creating order: {}", e.getMessage(), e);
            throw new RazorpayException("Failed to create Razorpay order: " + e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error while creating Razorpay order: {}", e.getMessage(), e);
            throw new PaymentException("Failed to create payment order. Please try again.", e);
        }
    }

    @Transactional
    public PaymentVerificationResponse verifyPayment(PaymentVerificationRequest request) {
        log.info("Verifying payment for razorpayOrderId: {}, paymentId: {}",
                request.getRazorpayOrderId(), request.getRazorpayPaymentId());

        try {
            Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Payment",
                            "razorpayOrderId",
                            request.getRazorpayOrderId()
                    ));

            log.info("Payment found in database: paymentId={}, status={}",
                    payment.getId(), payment.getStatus());

            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                log.warn("Payment already verified for razorpayOrderId: {}", request.getRazorpayOrderId());
                return buildSuccessResponse(payment);
            }

            boolean isValidSignature = false;

            if (testMode) {
                log.warn("TEST MODE: Skipping signature verification for razorpayOrderId: {}",
                        request.getRazorpayOrderId());
                isValidSignature = true;
            } else {
                log.info("PRODUCTION MODE: Verifying signature for razorpayOrderId: {}",
                        request.getRazorpayOrderId());

                JSONObject options = new JSONObject();
                options.put("razorpay_order_id", request.getRazorpayOrderId());
                options.put("razorpay_payment_id", request.getRazorpayPaymentId());
                options.put("razorpay_signature", request.getRazorpaySignature());

                isValidSignature = Utils.verifyPaymentSignature(options, keySecret);
            }

            if (isValidSignature) {
                log.info("Payment signature verified successfully for razorpayOrderId: {}",
                        request.getRazorpayOrderId());

                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
                payment.setRazorpaySignature(request.getRazorpaySignature());
                payment.setPaidAt(LocalDateTime.now());
                payment.setCompletedAt(LocalDateTime.now());
                payment.setUpdatedAt(LocalDateTime.now());

                Payment updatedPayment = paymentRepository.save(payment);
                log.info("Payment status updated to SUCCESS for paymentId: {}", updatedPayment.getId());

                Transaction transaction = Transaction.builder()
                        .payment(updatedPayment)
                        .type(TransactionType.PAYMENT)
                        .amount(updatedPayment.getAmount())
                        .status(PaymentStatus.SUCCESS)
                        .gatewayTransactionId(request.getRazorpayPaymentId())
                        .createdAt(LocalDateTime.now())
                        .build();

                transactionRepository.save(transaction);
                log.info("Success transaction record created for payment id: {}", updatedPayment.getId());

                updateOrderPaymentStatus(
                        updatedPayment.getOrderId(),
                        PaymentStatus.SUCCESS,
                        request.getRazorpayPaymentId()
                );

                return buildSuccessResponse(updatedPayment);

            } else {
                log.error("Payment signature verification FAILED for razorpayOrderId: {}",
                        request.getRazorpayOrderId());

                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Signature verification failed");
                payment.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(payment);

                Transaction transaction = Transaction.builder()
                        .payment(payment)
                        .type(TransactionType.PAYMENT)
                        .amount(payment.getAmount())
                        .status(PaymentStatus.FAILED)
                        .gatewayTransactionId(request.getRazorpayPaymentId())
                        .createdAt(LocalDateTime.now())
                        .build();

                transactionRepository.save(transaction);

                throw new PaymentException(
                        "Payment signature verification failed. Invalid signature.",
                        "SIGNATURE_VERIFICATION_FAILED"
                );
            }

        } catch (ResourceNotFoundException | PaymentException e) {
            log.error("Payment error: {}", e.getMessage());
            throw e;

        } catch (com.razorpay.RazorpayException e) {
            log.error("Razorpay API error during verification: {}", e.getMessage(), e);
            throw new RazorpayException("Razorpay verification failed: " + e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error during payment verification: {}", e.getMessage(), e);
            throw new PaymentException("Payment verification failed. Please contact support.", e);
        }
    }

    private void updateOrderPaymentStatus(Long orderId, PaymentStatus status, String razorpayPaymentId) {
        try {
            String webhookUrl = orderServiceUrl + "/api/orders/webhook/payment-status";
            log.info("Calling Order Service webhook: {} for orderId: {}", webhookUrl, orderId);

            Map<String, Object> webhookRequest = new HashMap<>();
            webhookRequest.put("orderId", orderId);
            webhookRequest.put("paymentStatus", status);
            webhookRequest.put("razorpayPaymentId", razorpayPaymentId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(webhookRequest, headers);

            restTemplate.postForEntity(webhookUrl, request, String.class);

            log.info("Order Service webhook called successfully for orderId: {}", orderId);

        } catch (Exception e) {
            log.error("Failed to update order payment status for orderId: {}. Error: {}",
                    orderId, e.getMessage(), e);
        }
    }

    public Payment getPaymentByRazorpayOrderId(String razorpayOrderId) {
        log.info("Fetching payment by razorpayOrderId: {}", razorpayOrderId);

        if (razorpayOrderId == null || razorpayOrderId.isBlank()) {
            throw new BadRequestException("Razorpay order ID cannot be null or empty");
        }

        return paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment",
                        "razorpayOrderId",
                        razorpayOrderId
                ));
    }

    public Payment getPaymentByOrderId(Long orderId) {
        log.info("Fetching payment by orderId: {}", orderId);

        if (orderId == null || orderId <= 0) {
            throw new BadRequestException("Order ID must be a positive number");
        }

        return paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment",
                        "orderId",
                        orderId
                ));
    }

    private void validateOrderRequest(PaymentOrderRequest request) {
        log.debug("Validating payment order request");

        if (request.getOrderId() == null || request.getOrderId() <= 0) {
            throw new BadRequestException("Order ID must be a positive number");
        }

        if (request.getUserId() == null || request.getUserId() <= 0) {
            throw new BadRequestException("User ID must be a positive number");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }

        if (request.getAmount().compareTo(new BigDecimal("1")) < 0) {
            throw new BadRequestException("Amount must be at least ₹1");
        }

        if (request.getAmount().compareTo(new BigDecimal("100000")) > 0) {
            throw new BadRequestException("Amount cannot exceed ₹1,00,000");
        }

        if (request.getCustomerName() == null || request.getCustomerName().isBlank()) {
            throw new BadRequestException("Customer name is required");
        }

        if (request.getCustomerEmail() == null ||
                !request.getCustomerEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BadRequestException("Valid email address is required");
        }

        if (request.getCustomerPhone() == null ||
                !request.getCustomerPhone().matches("^[6-9]\\d{9}$")) {
            throw new BadRequestException(
                    "Valid 10-digit mobile number starting with 6-9 is required"
            );
        }

        log.debug("Payment order request validation successful");
    }

    private int convertToRazorpayAmount(BigDecimal amount) {
        return amount.multiply(new BigDecimal("100")).intValue();
    }

    private PaymentVerificationResponse buildSuccessResponse(Payment payment) {
        return PaymentVerificationResponse.builder()
                .verified(true)
                .message("Payment verified successfully")
                .paymentId(payment.getRazorpayPaymentId())
                .orderId(payment.getRazorpayOrderId())
                .status(PaymentStatus.SUCCESS.name())
                .amount(payment.getAmount())
                .transactionId(payment.getRazorpayPaymentId())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
