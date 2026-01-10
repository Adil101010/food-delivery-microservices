package com.fooddelivery.supportservice.entity;

import com.fooddelivery.supportservice.enums.ResponderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "responder_id", nullable = false)
    private Long responderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "responder_type", nullable = false)
    private ResponderType responderType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "is_internal_note", nullable = false)
    private Boolean isInternalNote = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
