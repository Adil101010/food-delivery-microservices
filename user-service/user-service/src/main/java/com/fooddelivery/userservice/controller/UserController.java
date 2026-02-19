package com.fooddelivery.userservice.controller;

import com.fooddelivery.userservice.dto.UserResponse;
import com.fooddelivery.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
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

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        System.out.println("=== GET USER BY ID ===");
        System.out.println("Requested User ID: " + id);

        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestHeader("X-User-Id") Long userId) {
        System.out.println("=== GET CURRENT USER ===");
        System.out.println("User ID from header: " + userId);

        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        System.out.println("=== GET ALL USERS ===");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestHeader("X-User-Id") Long userId) {
        try {
            System.out.println("=== GET USER PROFILE ===");
            System.out.println("User ID from API Gateway header: " + userId);

            UserResponse user = userService.getUserById(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getUserId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("phone", user.getPhone());
            response.put("role", user.getRole().toString());
            response.put("active", user.getActive());
            response.put("createdAt", user.getCreatedAt());
            response.put("updatedAt", user.getUpdatedAt());

            System.out.println("Profile response: " + response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error fetching profile: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch profile");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, String> request) {
        try {
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Current and new password are required"
                ));
            }

            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Password must be at least 6 characters"
                ));
            }

            userService.changePassword(userId, currentPassword, newPassword);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Password changed successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }


    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, String> updates) {
        try {
            System.out.println("=== UPDATE USER PROFILE ===");
            System.out.println("User ID: " + userId);
            System.out.println("Updates: " + updates);

            UserResponse updatedUser = userService.updateUserProfile(userId, updates);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            response.put("id", updatedUser.getUserId());
            response.put("name", updatedUser.getName());
            response.put("email", updatedUser.getEmail());
            response.put("phone", updatedUser.getPhone());
            response.put("role", updatedUser.getRole().toString());

            System.out.println("Profile updated successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("success", "false");
            errorResponse.put("error", "Failed to update profile");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
