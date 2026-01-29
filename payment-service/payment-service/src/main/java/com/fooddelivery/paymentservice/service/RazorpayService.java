package com.fooddelivery.paymentservice.service;

import com.fooddelivery.paymentservice.dto.*;
import com.fooddelivery.paymentservice.entity.Payment;
import com.fooddelivery.paymentservice.enums.PaymentMethod;
import com.fooddelivery.paymentservice.enums.PaymentStatus;
import com.fooddelivery.paymentservice.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RazorpayService {

    private final RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.currency}")
    private String currency;

    @Value("${razorpay.company.name}")
    private String companyName;

    // Create Razorpay Order
    public PaymentOrderResponse createOrder(PaymentOrderRequest request) {
        try {
            log.info("Creating Razorpay order for orderId: {}, amount: {}",
                    request.getOrderId(), request.getAmount());

            // Create Razorpay order
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", request.getAmount() * 100); // Convert to paise
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", "order_" + request.getOrderId());

            Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");

            log.info("✅ Razorpay order created: {}", razorpayOrderId);

            // Save payment record in database
            Payment payment = Payment.builder()
                    .orderId(request.getOrderId())
                    .userId(request.getUserId())
                    .amount(request.getAmount())
                    .currency(currency)
                    .paymentMethod(PaymentMethod.RAZORPAY)
                    .status(PaymentStatus.PENDING)  // Use enum
                    .razorpayOrderId(razorpayOrderId)
                    .customerName(request.getCustomerName())
                    .customerEmail(request.getCustomerEmail())
                    .customerPhone(request.getCustomerPhone())
                    .build();

            paymentRepository.save(payment);

            // Create payment options for frontend
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
                            .amount(request.getAmount() * 100)
                            .currency(currency)
                            .name(companyName)
                            .description("Order #" + request.getOrderId())
                            .image("https://your-logo-url.com/logo.png")
                            .prefill(prefillData)
                            .callbackUrl("http://localhost:8089/api/payments/callback")
                            .build();

            // Create response
            return PaymentOrderResponse.builder()
                    .razorpayOrderId(razorpayOrderId)
                    .orderId(request.getOrderId())
                    .amount(request.getAmount())
                    .currency(currency)
                    .status("CREATED")
                    .keyId(keyId)
                    .customerName(request.getCustomerName())
                    .customerEmail(request.getCustomerEmail())
                    .customerPhone(request.getCustomerPhone())
                    .paymentOptions(paymentOptions)
                    .build();

        } catch (RazorpayException e) {
            log.error("Error creating Razorpay order: {}", e.getMessage());
            throw new RuntimeException("Failed to create payment order: " + e.getMessage());
        }
    }

    // Verify Payment Signature
    public PaymentVerificationResponse verifyPayment(PaymentVerificationRequest request) {
        try {
            log.info("Verifying payment for orderId: {}, paymentId: {}",
                    request.getRazorpayOrderId(), request.getRazorpayPaymentId());

            // Verify signature
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());

            boolean isValidSignature = Utils.verifyPaymentSignature(options, keySecret);

            if (isValidSignature) {
                log.info("Payment signature verified successfully");

                // Update payment in database
                Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                        .orElseThrow(() -> new RuntimeException("Payment not found"));

                payment.setStatus(PaymentStatus.SUCCESS);  //  Use enum
                payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
                payment.setRazorpaySignature(request.getRazorpaySignature());
                payment.setCompletedAt(LocalDateTime.now());
                payment.setPaidAt(LocalDateTime.now());

                paymentRepository.save(payment);

                return PaymentVerificationResponse.builder()
                        .verified(true)
                        .message("Payment verified successfully")
                        .paymentId(request.getRazorpayPaymentId())
                        .orderId(request.getRazorpayOrderId())
                        .status("SUCCESS")
                        .amount(payment.getAmount())
                        .transactionId(request.getRazorpayPaymentId())
                        .build();

            } else {
                log.error("Payment signature verification failed");

                // Update payment as failed
                Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                        .orElse(null);

                if (payment != null) {
                    payment.setStatus(PaymentStatus.FAILED);  //  Use enum
                    payment.setFailureReason("Signature verification failed");
                    paymentRepository.save(payment);
                }

                return PaymentVerificationResponse.builder()
                        .verified(false)
                        .message("Payment verification failed")
                        .status("FAILED")
                        .build();
            }

        } catch (RazorpayException e) {
            log.error("Error verifying payment: {}", e.getMessage());
            throw new RuntimeException("Payment verification failed: " + e.getMessage());
        }
    }

    // Get Payment by Razorpay Order ID
    public Payment getPaymentByRazorpayOrderId(String razorpayOrderId) {
        return paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    // Get Payment by Order ID
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
    }
}
