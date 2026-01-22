package com.fooddelivery.adminservice.controller;

import com.fooddelivery.adminservice.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    // Health Check
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        return ResponseEntity.ok(new MessageResponse("Admin Service is running"));
    }
}
