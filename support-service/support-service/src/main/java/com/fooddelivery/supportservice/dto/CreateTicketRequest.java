package com.fooddelivery.supportservice.dto;

import com.fooddelivery.supportservice.enums.TicketCategory;
import com.fooddelivery.supportservice.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Category is required")
    private TicketCategory category;

    private TicketPriority priority;

    private Long orderId;
}
