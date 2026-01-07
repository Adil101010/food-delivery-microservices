package com.fooddelivery.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    // For now, just log emails (can integrate real email later)
    public boolean sendEmail(String to, String subject, String body) {
        try {
            log.info("===========================================");
            log.info("📧 EMAIL SENT");
            log.info("To: {}", to);
            log.info("Subject: {}", subject);
            log.info("Body: {}", body);
            log.info("===========================================");
            return true;
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
            return false;
        }
    }
}
