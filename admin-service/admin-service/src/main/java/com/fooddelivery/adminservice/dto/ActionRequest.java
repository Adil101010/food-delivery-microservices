package com.fooddelivery.adminservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionRequest {
    @NotNull(message = "ID is required")
    private Long id;

    private String reason; // For rejection/blocking
    private String action; // APPROVE, REJECT, BLOCK, UNBLOCK
}
