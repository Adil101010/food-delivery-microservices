package com.fooddelivery.notificationservice.controller;

import com.fooddelivery.notificationservice.dto.MessageResponse;
import com.fooddelivery.notificationservice.dto.NotificationResponse;
import com.fooddelivery.notificationservice.dto.SendNotificationRequest;
import com.fooddelivery.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    // Health Check
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        return ResponseEntity.ok(new MessageResponse("Notification Service is running"));
    }

    // Send Notification
    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        NotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get User Notifications
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(
            @PathVariable Long userId) {
        List<NotificationResponse> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }


    // Get Notification by ID
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long id) {
        NotificationResponse notification = notificationService.getNotificationById(id);
        return ResponseEntity.ok(notification);
    }

    // Get Order Notifications
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<NotificationResponse>> getOrderNotifications(
            @PathVariable Long orderId) {
        List<NotificationResponse> notifications = notificationService.getOrderNotifications(orderId);
        return ResponseEntity.ok(notifications);
    }
}
