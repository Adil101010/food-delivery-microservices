package com.fooddelivery.assignmentservice.entity;

import com.fooddelivery.assignmentservice.enums.AssignmentStatus;
import com.fooddelivery.assignmentservice.enums.AssignmentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false)
    private Long restaurantId;

    @Column(nullable = false)
    private Long customerId;

    private Long partnerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status = AssignmentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentType type = AssignmentType.AUTO;

    private Double restaurantLatitude;

    private Double restaurantLongitude;

    private Double customerLatitude;

    private Double customerLongitude;

    private Double distance; // km

    private Integer estimatedTime; // minutes

    private Integer attemptCount = 0; // How many times assignment tried

    private String rejectionReason;

    private LocalDateTime assignedAt;

    private LocalDateTime acceptedAt;

    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
