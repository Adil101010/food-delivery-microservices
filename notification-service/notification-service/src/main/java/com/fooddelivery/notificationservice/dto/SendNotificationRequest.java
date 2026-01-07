package com.fooddelivery.notificationservice.dto;

import com.fooddelivery.notificationservice.enums.NotificationTemplate;
import com.fooddelivery.notificationservice.enums.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotNull(message = "Template is required")
    private NotificationTemplate template;

    @NotNull(message = "Recipient is required")
    private String recipient; // Email or Phone

    private Long orderId;

    private Map<String, String> templateData; // Dynamic data for template
}
