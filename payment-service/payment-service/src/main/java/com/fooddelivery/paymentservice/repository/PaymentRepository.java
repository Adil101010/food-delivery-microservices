package com.fooddelivery.paymentservice.repository;

import com.fooddelivery.paymentservice.entity.Payment;
import com.fooddelivery.paymentservice.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByUserId(Long userId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByUserIdAndStatus(Long userId, PaymentStatus status);

    // Razorpay specific methods
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    // Get all payments for an order (in case of multiple attempts)
    List<Payment> findAllByOrderId(Long orderId);

    // Get latest payment by order ID
    Optional<Payment> findFirstByOrderIdOrderByCreatedAtDesc(Long orderId);
}
