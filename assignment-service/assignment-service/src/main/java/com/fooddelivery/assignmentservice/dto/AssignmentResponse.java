package com.fooddelivery.assignmentservice.dto;

import com.fooddelivery.assignmentservice.enums.AssignmentStatus;
import com.fooddelivery.assignmentservice.enums.AssignmentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {

    private Long id;
    private Long orderId;
    private Long restaurantId;
    private Long customerId;
    private Long partnerId;
    private AssignmentStatus status;
    private AssignmentType type;
    private Double distance;
    private Integer estimatedTime;
    private Integer attemptCount;
    private String rejectionReason;
    private LocalDateTime assignedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
