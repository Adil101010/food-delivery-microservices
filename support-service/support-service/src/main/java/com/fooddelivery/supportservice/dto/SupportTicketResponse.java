package com.fooddelivery.supportservice.dto;

import com.fooddelivery.supportservice.enums.TicketCategory;
import com.fooddelivery.supportservice.enums.TicketPriority;
import com.fooddelivery.supportservice.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketResponse {
    private Long id;
    private String ticketNumber;
    private Long userId;
    private String subject;
    private String description;
    private TicketCategory category;
    private TicketPriority priority;
    private TicketStatus status;
    private Long assignedTo;
    private Long orderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
}
