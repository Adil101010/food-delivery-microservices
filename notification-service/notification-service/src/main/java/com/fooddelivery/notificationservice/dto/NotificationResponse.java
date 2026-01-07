package com.fooddelivery.notificationservice.dto;

import com.fooddelivery.notificationservice.enums.NotificationStatus;
import com.fooddelivery.notificationservice.enums.NotificationTemplate;
import com.fooddelivery.notificationservice.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private Long userId;
    private NotificationType type;
    private NotificationTemplate template;
    private String recipient;
    private String subject;
    private String message;
    private NotificationStatus status;
    private Long orderId;
    private String errorMessage;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
