package com.fooddelivery.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    // For now, just log SMS (can integrate real SMS later)
    public boolean sendSms(String phone, String message) {
        try {
            log.info("===========================================");
            log.info("📱 SMS SENT");
            log.info("To: {}", phone);
            log.info("Message: {}", message);
            log.info("===========================================");
            return true;
        } catch (Exception e) {
            log.error("Failed to send SMS: {}", e.getMessage());
            return false;
        }
    }
}
