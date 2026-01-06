package com.fooddelivery.orderservice.entity;

import com.fooddelivery.orderservice.enums.OrderStatus;
import com.fooddelivery.orderservice.enums.PaymentMethod;
import com.fooddelivery.orderservice.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long restaurantId;

    @Column(nullable = false)
    private String restaurantName;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private Double subtotal;

    @Column(nullable = false)
    private Double deliveryFee;

    private Double tax;

    private Double discount;

    @Column(nullable = false)
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private String deliveryAddress;

    private String deliveryInstructions;

    @Column(nullable = false)
    private String customerPhone;

    private String customerName;

    private LocalDateTime estimatedDeliveryTime;

    private LocalDateTime deliveredAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Helper method to add items
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    // Helper method to remove items
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
}
