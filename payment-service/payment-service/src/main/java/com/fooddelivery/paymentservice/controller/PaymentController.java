package com.fooddelivery.paymentservice.controller;

import com.fooddelivery.paymentservice.dto.*;
import com.fooddelivery.paymentservice.entity.Payment;
import com.fooddelivery.paymentservice.service.PaymentService;
import com.fooddelivery.paymentservice.service.RazorpayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final RazorpayService razorpayService;

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

    // ============================================
    // RAZORPAY INTEGRATION ENDPOINTS
    // ============================================

    // Create Razorpay Order
    @PostMapping("/razorpay/create-order")
    public ResponseEntity<PaymentOrderResponse> createRazorpayOrder(
            @Valid @RequestBody PaymentOrderRequest request) {

        log.info("Creating Razorpay payment order: {}", request);

        try {
            PaymentOrderResponse response = razorpayService.createOrder(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating Razorpay order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Verify Razorpay Payment
    @PostMapping("/razorpay/verify")
    public ResponseEntity<PaymentVerificationResponse> verifyRazorpayPayment(
            @Valid @RequestBody PaymentVerificationRequest request) {

        log.info("Verifying Razorpay payment: {}", request);

        try {
            PaymentVerificationResponse response = razorpayService.verifyPayment(request);

            if (response.getVerified()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("Error verifying Razorpay payment: {}", e.getMessage());

            PaymentVerificationResponse errorResponse = PaymentVerificationResponse.builder()
                    .verified(false)
                    .message("Payment verification failed: " + e.getMessage())
                    .status("FAILED")
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Verify Razorpay Payment (Frontend endpoint) - ADD THIS
    @PostMapping("/razorpay/verify-payment")
    public ResponseEntity<PaymentVerificationResponse> verifyRazorpayPaymentFrontend(
            @Valid @RequestBody PaymentVerificationRequest request) {

        log.info("Verifying Razorpay payment from frontend: {}", request);

        try {
            PaymentVerificationResponse response = razorpayService.verifyPayment(request);

            if (response.getVerified()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("Error verifying Razorpay payment: {}", e.getMessage());

            PaymentVerificationResponse errorResponse = PaymentVerificationResponse.builder()
                    .verified(false)
                    .message("Payment verification failed: " + e.getMessage())
                    .status("FAILED")
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Get Payment by Razorpay Order ID
    @GetMapping("/razorpay/order/{razorpayOrderId}")
    public ResponseEntity<Payment> getPaymentByRazorpayOrderId(
            @PathVariable String razorpayOrderId) {

        try {
            Payment payment = razorpayService.getPaymentByRazorpayOrderId(razorpayOrderId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            log.error("Payment not found for Razorpay order: {}", razorpayOrderId);
            return ResponseEntity.notFound().build();
        }
    }

    // Get Payment by Food Order ID
    @GetMapping("/razorpay/food-order/{orderId}")
    public ResponseEntity<Payment> getPaymentByFoodOrderId(@PathVariable Long orderId) {
        try {
            Payment payment = razorpayService.getPaymentByOrderId(orderId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            log.error("Payment not found for order: {}", orderId);
            return ResponseEntity.notFound().build();
        }
    }

    // Razorpay Payment Callback
    @PostMapping("/razorpay/callback")
    public ResponseEntity<Map<String, String>> razorpayCallback(
            @RequestParam("razorpay_order_id") String razorpayOrderId,
            @RequestParam("razorpay_payment_id") String razorpayPaymentId,
            @RequestParam("razorpay_signature") String razorpaySignature) {

        log.info("Razorpay callback received: orderId={}, paymentId={}",
                razorpayOrderId, razorpayPaymentId);

        PaymentVerificationRequest request = new PaymentVerificationRequest(
                razorpayOrderId, razorpayPaymentId, razorpaySignature, null
        );

        PaymentVerificationResponse response = razorpayService.verifyPayment(request);

        Map<String, String> result = new HashMap<>();
        result.put("status", response.getVerified() ? "success" : "failed");
        result.put("message", response.getMessage());

        return ResponseEntity.ok(result);
    }

    // Success Page (for redirect)
    @GetMapping("/razorpay/success")
    public ResponseEntity<Map<String, String>> razorpaySuccess() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Payment completed successfully!");
        return ResponseEntity.ok(response);
    }

    // Failure Page (for redirect)
    @GetMapping("/razorpay/failure")
    public ResponseEntity<Map<String, String>> razorpayFailure() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "failed");
        response.put("message", "Payment failed. Please try again.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
