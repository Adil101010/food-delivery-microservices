package com.fooddelivery.authservice.controller;

import com.fooddelivery.authservice.dto.*;
import com.fooddelivery.authservice.enums.OtpType;
import com.fooddelivery.authservice.security.JwtUtil;
import com.fooddelivery.authservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    // Health Check
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        return ResponseEntity.ok(new MessageResponse("Auth Service is running"));
    }

    // Register User
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Login User
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthResponse response = authService.login(request, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    // Refresh Access Token
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    // Logout User
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody RefreshTokenRequest request) {
        MessageResponse response = authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    // Validate Token (for other services to call)
    @PostMapping("/validate-token")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestBody TokenValidationRequest request) {
        boolean isValid = jwtUtil.validateToken(request.getToken());

        if (!isValid) {
            return ResponseEntity.ok(TokenValidationResponse.builder()
                    .valid(false)
                    .build());
        }

        Long userId = jwtUtil.extractUserId(request.getToken());
        String email = jwtUtil.extractEmail(request.getToken());
        String role = jwtUtil.extractRole(request.getToken());

        return ResponseEntity.ok(TokenValidationResponse.builder()
                .valid(true)
                .userId(userId)
                .email(email)
                .role(role)
                .build());
    }

    // Change Password
    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ChangePasswordRequest request) {

        Long userId = extractUserIdFromToken(authHeader);
        MessageResponse response = authService.changePassword(userId, request);
        return ResponseEntity.ok(response);
    }

    // Forgot Password
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        MessageResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    // Reset Password
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        MessageResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    // Send Email Verification OTP
    @PostMapping("/send-email-otp")
    public ResponseEntity<MessageResponse> sendEmailOtp(@RequestParam String email) {
        MessageResponse response = authService.generateOtp(email, null, OtpType.EMAIL_VERIFICATION);
        return ResponseEntity.ok(response);
    }

    // Send Phone Verification OTP
    @PostMapping("/send-phone-otp")
    public ResponseEntity<MessageResponse> sendPhoneOtp(@RequestParam String phone) {
        MessageResponse response = authService.generateOtp(null, phone, OtpType.PHONE_VERIFICATION);
        return ResponseEntity.ok(response);
    }

    // Verify OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<MessageResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        MessageResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    // Get Current User Info (from token)
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");

        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = jwtUtil.extractUserId(token);
        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractRole(token);

        UserInfoResponse response = UserInfoResponse.builder()
                .userId(userId)
                .email(email)
                .role(role)
                .build();

        return ResponseEntity.ok(response);
    }

    // Helper: Extract User ID from Authorization Header
    private Long extractUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.extractUserId(token);
    }

    // Helper: Get Client IP Address
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
