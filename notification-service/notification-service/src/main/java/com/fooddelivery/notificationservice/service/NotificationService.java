package com.fooddelivery.notificationservice.service;

import com.fooddelivery.notificationservice.dto.NotificationResponse;
import com.fooddelivery.notificationservice.dto.SendNotificationRequest;
import com.fooddelivery.notificationservice.entity.Notification;
import com.fooddelivery.notificationservice.enums.NotificationStatus;
import com.fooddelivery.notificationservice.enums.NotificationTemplate;
import com.fooddelivery.notificationservice.enums.NotificationType;
import com.fooddelivery.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    // Send Notification
    @Transactional
    public NotificationResponse sendNotification(SendNotificationRequest request) {

        // Generate subject and message from template
        String subject = generateSubject(request.getTemplate(), request.getTemplateData());
        String message = generateMessage(request.getTemplate(), request.getTemplateData());

        // Create Notification Entity
        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setType(request.getType());
        notification.setTemplate(request.getTemplate());
        notification.setRecipient(request.getRecipient());
        notification.setSubject(subject);
        notification.setMessage(message);
        notification.setOrderId(request.getOrderId());
        notification.setStatus(NotificationStatus.PENDING);

        // Save to database
        Notification savedNotification = notificationRepository.save(notification);

        // Send notification based on type
        boolean sent = false;
        try {
            if (request.getType() == NotificationType.EMAIL) {
                sent = emailService.sendEmail(request.getRecipient(), subject, message);
            } else if (request.getType() == NotificationType.SMS) {
                sent = smsService.sendSms(request.getRecipient(), message);
            }

            if (sent) {
                savedNotification.setStatus(NotificationStatus.SENT);
                savedNotification.setSentAt(LocalDateTime.now());
            } else {
                savedNotification.setStatus(NotificationStatus.FAILED);
                savedNotification.setErrorMessage("Failed to send notification");
            }

        } catch (Exception e) {
            savedNotification.setStatus(NotificationStatus.FAILED);
            savedNotification.setErrorMessage(e.getMessage());
        }

        notificationRepository.save(savedNotification);

        return convertToResponse(savedNotification);
    }

    // Get User Notifications
    public List<NotificationResponse> getUserNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserId(userId);
        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get Notification by ID
    public NotificationResponse getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        return convertToResponse(notification);
    }

    // Get Order Notifications
    public List<NotificationResponse> getOrderNotifications(Long orderId) {
        List<Notification> notifications = notificationRepository.findByOrderId(orderId);
        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Generate Subject from Template
    private String generateSubject(NotificationTemplate template, Map<String, String> data) {
        return switch (template) {
            case ORDER_CREATED -> "Order Confirmed - #" + data.getOrDefault("orderId", "N/A");
            case ORDER_CONFIRMED -> "Restaurant Accepted Your Order - #" + data.getOrDefault("orderId", "N/A");
            case ORDER_PREPARING -> "Your Food is Being Prepared - #" + data.getOrDefault("orderId", "N/A");
            case ORDER_OUT_FOR_DELIVERY -> "Your Order is On The Way! - #" + data.getOrDefault("orderId", "N/A");
            case ORDER_DELIVERED -> "Order Delivered Successfully - #" + data.getOrDefault("orderId", "N/A");
            case ORDER_CANCELLED -> "Order Cancelled - #" + data.getOrDefault("orderId", "N/A");
            case USER_REGISTERED -> "Welcome to Food Delivery Platform!";
            case PAYMENT_SUCCESS -> "Payment Successful - ₹" + data.getOrDefault("amount", "0");
            case PAYMENT_FAILED -> "Payment Failed - Please Retry";
        };
    }

    // Generate Message from Template
    private String generateMessage(NotificationTemplate template, Map<String, String> data) {
        return switch (template) {
            case ORDER_CREATED -> String.format(
                    "Hi %s,\n\nYour order #%s has been placed successfully!\n\nRestaurant: %s\nTotal Amount: ₹%s\nEstimated Delivery: %s\n\nThank you for ordering with us!",
                    data.getOrDefault("customerName", "Customer"),
                    data.getOrDefault("orderId", "N/A"),
                    data.getOrDefault("restaurantName", "N/A"),
                    data.getOrDefault("totalAmount", "0"),
                    data.getOrDefault("estimatedTime", "45 minutes")
            );
            case ORDER_CONFIRMED -> String.format(
                    "Good news! %s has accepted your order #%s.\n\nYour food is being prepared and will be delivered soon.",
                    data.getOrDefault("restaurantName", "Restaurant"),
                    data.getOrDefault("orderId", "N/A")
            );
            case ORDER_PREPARING -> String.format(
                    "Your order #%s is being prepared by %s.\n\nEstimated delivery: %s",
                    data.getOrDefault("orderId", "N/A"),
                    data.getOrDefault("restaurantName", "Restaurant"),
                    data.getOrDefault("estimatedTime", "30 minutes")
            );
            case ORDER_OUT_FOR_DELIVERY -> String.format(
                    "Your order #%s is out for delivery!\n\nDelivery Partner: %s\nPhone: %s\n\nTrack your order in real-time.",
                    data.getOrDefault("orderId", "N/A"),
                    data.getOrDefault("deliveryPartner", "Partner"),
                    data.getOrDefault("partnerPhone", "N/A")
            );
            case ORDER_DELIVERED -> String.format(
                    "Your order #%s has been delivered!\n\nWe hope you enjoyed your meal. Please rate your experience.",
                    data.getOrDefault("orderId", "N/A")
            );
            case ORDER_CANCELLED -> String.format(
                    "Your order #%s has been cancelled.\n\nReason: %s\n\nRefund will be processed within 3-5 business days.",
                    data.getOrDefault("orderId", "N/A"),
                    data.getOrDefault("reason", "Customer request")
            );
            case USER_REGISTERED -> String.format(
                    "Welcome to Food Delivery Platform, %s!\n\nYour account has been created successfully.\n\nStart ordering delicious food from your favorite restaurants.",
                    data.getOrDefault("userName", "User")
            );
            case PAYMENT_SUCCESS -> String.format(
                    "Payment of ₹%s received successfully!\n\nTransaction ID: %s\nOrder #%s",
                    data.getOrDefault("amount", "0"),
                    data.getOrDefault("transactionId", "N/A"),
                    data.getOrDefault("orderId", "N/A")
            );
            case PAYMENT_FAILED -> String.format(
                    "Payment of ₹%s failed.\n\nReason: %s\n\nPlease try again or use a different payment method.",
                    data.getOrDefault("amount", "0"),
                    data.getOrDefault("reason", "Payment declined")
            );
        };
    }

    // Convert Entity to Response
    private NotificationResponse convertToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setUserId(notification.getUserId());
        response.setType(notification.getType());
        response.setTemplate(notification.getTemplate());
        response.setRecipient(notification.getRecipient());
        response.setSubject(notification.getSubject());
        response.setMessage(notification.getMessage());
        response.setStatus(notification.getStatus());
        response.setOrderId(notification.getOrderId());
        response.setErrorMessage(notification.getErrorMessage());
        response.setSentAt(notification.getSentAt());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
}
