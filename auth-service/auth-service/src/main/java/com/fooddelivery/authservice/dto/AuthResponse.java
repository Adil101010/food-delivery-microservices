package com.fooddelivery.authservice.dto;

import com.fooddelivery.authservice.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private Long userId;
    private String email;
    private String phone;
    private UserRole role;
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiresIn;
    private Long refreshTokenExpiresIn;
    private Boolean isEmailVerified;
    private Boolean isPhoneVerified;
}
