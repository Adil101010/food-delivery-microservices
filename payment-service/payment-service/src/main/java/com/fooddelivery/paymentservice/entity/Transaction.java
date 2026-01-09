package com.fooddelivery.paymentservice.entity;

import com.fooddelivery.paymentservice.enums.PaymentStatus;
import com.fooddelivery.paymentservice.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long paymentId;

    @Column(nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false, unique = true)
    private String transactionId;

    @Column(length = 1000)
    private String gatewayResponse;

    private String message;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
