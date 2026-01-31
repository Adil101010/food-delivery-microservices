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

    @GetMapping("/health")
    public ResponseEntity<com.fooddelivery.paymentservice.dto.ApiResponse<MessageResponse>> healthCheck() {
        log.info("Health check requested");
        MessageResponse message = new MessageResponse("Payment Service is running");
        return ResponseEntity.ok(com.fooddelivery.paymentservice.dto.ApiResponse.success("Service is healthy", message));
    }

    @PostMapping("/create")
    public ResponseEntity<com.fooddelivery.paymentservice.dto.ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {
        log.info("Creating payment for orderId: {}, userId: {}", request.getOrderId(), request.getUserId());
        PaymentResponse payment = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                com.fooddelivery.paymentservice.dto.ApiResponse.success("Payment created successfully", payment));
    }

    @PostMapping("/{paymentId}/process")
    public ResponseEntity<com.fooddelivery.paymentservice.dto.ApiResponse<PaymentResponse>> processPayment(
            @PathVariable Long paymentId) {
        log.info("Processing payment: {}", paymentId);
        PaymentResponse payment = paymentService.processPayment(paymentId);
        return ResponseEntity.ok(com.fooddelivery.paymentservice.dto.ApiResponse.success("Payment processed successfully", payment));
    }

    @PostMapping("/verify")
    public ResponseEntity<com.fooddelivery.paymentservice.dto.ApiResponse<PaymentResponse>> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request) {
        log.info("Verifying payment for paymentId: {}", request.getPaymentId());
        PaymentResponse payment = paymentService.verifyPayment(request);
        return ResponseEntity.ok(com.fooddelivery.paymentservice.dto.ApiResponse.success("Payment verified successfully", payment));
    }

    @PostMapping("/refund")
    public ResponseEntity<com.fooddelivery.paymentservice.dto.ApiResponse<PaymentResponse>> processRefund(
            @Valid @RequestBody RefundRequest request) {
        log.info("Processing refund for paymentId: {}", request.getPaymentId());
        PaymentResponse payment = paymentService.processRefund(request);
        return ResponseEntity.ok(com.fooddelivery.paymentservice.dto.ApiResponse.success("Refund processed successfully", payment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<com.fooddelivery.paymentservice.dto.ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
        log.info("Fetching payment by id: {}", id);
        PaymentResponse payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(com.fooddelivery.paymentservice.dto.ApiResponse.success("Payment retrieved successfully", payment));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<com.fooddelivery.paymentservice.dto.ApiResponse<PaymentResponse>> getPaymentByOrderId(@PathVariable Long orderId) {
        log.info("Fetching payment by orderId: {}", orderId);
        PaymentResponse payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(com.fooddelivery.paymentservice.dto.ApiResponse.success("Payment retrieved successfully", payment));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<com.fooddelivery.paymentservice.dto.ApiResponse<List<PaymentResponse>>> getUserPayments(@PathVariable Long userId) {
        log.info("Fetching payments for userId: {}", userId);
        List<PaymentResponse> payments = paymentService.getUserPayments(userId);
        return ResponseEntity.ok(com.fooddelivery.paymentservice.dto.ApiResponse.success("User payments retrieved successfully", payments));
    }

    @GetMapping("/{paymentId}/transactions")
    public ResponseEntity<com.fooddelivery.paymentservice.dto.ApiResponse<List<TransactionResponse>>> getPaymentTransactions(@PathVariable Long paymentId) {
        log.info("Fetching transactions for paymentId: {}", paymentId);
        List<TransactionResponse> transactions = paymentService.getPaymentTransactions(paymentId);
        return ResponseEntity.ok(com.fooddelivery.paymentservice.dto.ApiResponse.success("Transaction history retrieved successfully", transactions));
    }

    @PostMapping("/razorpay/create-order")
    public ResponseEntity<com.fooddelivery.paymentservice.dto.ApiResponse<PaymentOrderResponse>> createRazorpayOrder(
            @Valid @RequestBody PaymentOrderRequest request) {
        log.info("Creating Razorpay order for orderId: {}, amount: {}", request.getOrderId(), request.getAmount());
        PaymentOrderResponse response = razorpayService.createOrder(request);
        return ResponseEntity.ok(com.fooddelivery.paymentservice.dto.ApiResponse.success("Razorpay order created successfully", response));
    }

    @PostMapping("/razorpay/verify-payment")
    public ResponseEntity<com.fooddelivery.paymentservice.dto.ApiResponse<PaymentVerificationResponse>> verifyRazorpayPayment(
            @Valid @RequestBody PaymentVerificationRequest request) {
        log.info("Verifying Razorpay payment for orderId: {}, paymentId: {}", request.getRazorpayOrderId(), request.getRazorpayPaymentId());
        PaymentVerificationResponse response = razorpayService.verifyPayment(request);
        return ResponseEntity.ok(com.fooddelivery.paymentservice.dto.ApiResponse.success("Payment verified successfully", response));
    }

    @PostMapping("/razorpay/verify")
    public ResponseEntity<com.fooddelivery.paymentservice.dto.ApiResponse<PaymentVerificationResponse>> verifyRazorpayPaymentAlternative(
            @Valid @RequestBody PaymentVerificationRequest request) {
        log.info("Verifying Razorpay payment (alternative endpoint) for orderId: {}", request.getRazorpayOrderId());
        PaymentVerificationResponse response = razorpayService.verifyPayment(request);
        return ResponseEntity.ok(com.fooddelivery.paymentservice.dto.ApiResponse.success("Payment verified successfully", response));
    }

    @GetMapping("/razorpay/order/{razorpayOrderId}")
    public ResponseEntity<com.fooddelivery.paymentservice.dto.ApiResponse<Payment>> getPaymentByRazorpayOrderId(@PathVariable String razorpayOrderId) {
        log.info("Fetching payment by razorpayOrderId: {}", razorpayOrderId);
        Payment payment = razorpayService.getPaymentByRazorpayOrderId(razorpayOrderId);
        return ResponseEntity.ok(com.fooddelivery.paymentservice.dto.ApiResponse.success("Payment retrieved successfully", payment));
    }

    @GetMapping("/razorpay/food-order/{orderId}")
    public ResponseEntity<com.fooddelivery.paymentservice.dto.ApiResponse<Payment>> getPaymentByFoodOrderId(@PathVariable Long orderId) {
        log.info("Fetching payment by food orderId: {}", orderId);
        Payment payment = razorpayService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(com.fooddelivery.paymentservice.dto.ApiResponse.success("Payment retrieved successfully", payment));
    }

    @PostMapping("/razorpay/callback")
    public ResponseEntity<com.fooddelivery.paymentservice.dto.ApiResponse<Map<String, String>>> razorpayCallback(
            @RequestParam("razorpay_order_id") String razorpayOrderId,
            @RequestParam("razorpay_payment_id") String razorpayPaymentId,
            @RequestParam("razorpay_signature") String razorpaySignature) {
        log.info("Razorpay callback received - orderId: {}, paymentId: {}", razorpayOrderId, razorpayPaymentId);

        PaymentVerificationRequest request = PaymentVerificationRequest.builder()
                .razorpayOrderId(razorpayOrderId)
                .razorpayPaymentId(razorpayPaymentId)
                .razorpaySignature(razorpaySignature)
                .build();

        PaymentVerificationResponse response = razorpayService.verifyPayment(request);

        Map<String, String> result = new HashMap<>();
        result.put("status", response.getVerified() ? "SUCCESS" : "FAILED");
        result.put("message", response.getMessage());
        result.put("paymentId", response.getPaymentId());
        result.put("orderId", response.getOrderId());

        return ResponseEntity.ok(com.fooddelivery.paymentservice.dto.ApiResponse.success(response.getMessage(), result));
    }

    @GetMapping("/razorpay/success")
    public ResponseEntity<com.fooddelivery.paymentservice.dto.ApiResponse<Map<String, String>>> razorpaySuccess(
            @RequestParam(required = false) String paymentId,
            @RequestParam(required = false) String orderId) {
        log.info("Payment success page accessed - paymentId: {}, orderId: {}", paymentId, orderId);

        Map<String, String> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Payment completed successfully!");
        response.put("title", "Payment Successful");

        if (paymentId != null) response.put("paymentId", paymentId);
        if (orderId != null) response.put("orderId", orderId);

        return ResponseEntity.ok(com.fooddelivery.paymentservice.dto.ApiResponse.success("Payment successful", response));
    }

    @GetMapping("/razorpay/failure")
    public ResponseEntity<com.fooddelivery.paymentservice.dto.ApiResponse<Map<String, String>>> razorpayFailure(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String orderId) {
        log.warn("Payment failure page accessed - error: {}, orderId: {}", error, orderId);

        Map<String, String> response = new HashMap<>();
        response.put("status", "FAILED");
        response.put("message", "Payment failed. Please try again.");
        response.put("title", "Payment Failed");

        if (error != null) response.put("error", error);
        if (orderId != null) response.put("orderId", orderId);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(com.fooddelivery.paymentservice.dto.ApiResponse.error("Payment failed"));
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<com.fooddelivery.paymentservice.dto.ApiResponse<MessageResponse>> cancelPayment(@PathVariable Long paymentId) {
        log.info("Cancelling payment: {}", paymentId);
        MessageResponse message = new MessageResponse("Payment cancellation feature coming soon");
        return ResponseEntity.ok(com.fooddelivery.paymentservice.dto.ApiResponse.success("Request processed", message));
    }
}
