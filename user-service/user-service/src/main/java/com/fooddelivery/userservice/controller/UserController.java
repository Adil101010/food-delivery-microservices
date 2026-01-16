package com.fooddelivery.userservice.controller;

import com.fooddelivery.userservice.dto.UserResponse;  // ← ADD THIS
import com.fooddelivery.userservice.entity.User;
import com.fooddelivery.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;  // ← ADD THIS
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/test-headers")
    public ResponseEntity<Map<String, String>> testHeaders(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        Map<String, String> headers = new HashMap<>();
        headers.put("userId", userId);
        headers.put("email", email);
        headers.put("role", role);
        return ResponseEntity.ok(headers);
    }

    // ← UPDATED: Return UserResponse instead of User
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        System.out.println("=== GET USER BY ID ===");
        System.out.println("Requested User ID: " + id);

        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    // ← UPDATED: Return UserResponse instead of User
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestHeader("X-User-Id") Long userId) {
        System.out.println("=== GET CURRENT USER ===");
        System.out.println("User ID from header: " + userId);

        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    // ← UPDATED: Return List<UserResponse> instead of List<User>
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        System.out.println("=== GET ALL USERS ===");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
