package com.fooddelivery.supportservice.dto;

import com.fooddelivery.supportservice.enums.ResponderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDto {
    private Long id;
    private Long ticketId;
    private Long responderId;
    private ResponderType responderType;
    private String message;
    private Boolean isInternalNote;
    private LocalDateTime createdAt;
}
