package com.fooddelivery.supportservice.dto;

import com.fooddelivery.supportservice.enums.ResponderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddResponseRequest {

    @NotNull(message = "Responder ID is required")
    private Long responderId;

    @NotNull(message = "Responder type is required")
    private ResponderType responderType;

    @NotBlank(message = "Message is required")
    private String message;

    private Boolean isInternalNote = false;
}
