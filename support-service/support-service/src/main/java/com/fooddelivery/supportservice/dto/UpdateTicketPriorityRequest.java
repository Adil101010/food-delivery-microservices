package com.fooddelivery.supportservice.dto;

import com.fooddelivery.supportservice.enums.TicketPriority;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTicketPriorityRequest {

    @NotNull(message = "Priority is required")
    private TicketPriority priority;
}
