package com.fooddelivery.orderservice.entity;

import com.fooddelivery.orderservice.enums.OrderStatus;
import com.fooddelivery.orderservice.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", length = 25, nullable = false)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 25, nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "payment_method", length = 25)
    private String paymentMethod;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long menuItemId;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double price; // Price per item

    @Column(nullable = false)
    private Double subtotal; // quantity * price

    private String specialInstructions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
}
