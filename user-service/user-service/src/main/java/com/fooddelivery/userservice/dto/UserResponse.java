package com.fooddelivery.userservice.dto;

import com.fooddelivery.userservice.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String email;
    private String name;
    private String phone;
    private UserRole role;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String address;  // Simple String for now
}
