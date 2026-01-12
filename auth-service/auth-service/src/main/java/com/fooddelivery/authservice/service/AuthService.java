package com.fooddelivery.authservice.service;

import com.fooddelivery.authservice.dto.*;
import com.fooddelivery.authservice.entity.*;
import com.fooddelivery.authservice.enums.LoginStatus;
import com.fooddelivery.authservice.enums.OtpType;
import com.fooddelivery.authservice.exception.*;
import com.fooddelivery.authservice.repository.*;
import com.fooddelivery.authservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpRepository otpRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${otp.expiration-minutes}")
    private Integer otpExpirationMinutes;

    @Value("${account.max-failed-login-attempts}")
    private Integer maxFailedLoginAttempts;

    @Value("${account.lock-duration-minutes}")
    private Integer lockDurationMinutes;

    // Register User
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("Email already registered");
        }

        // Check if phone already exists
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new CustomException("Phone number already registered");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setIsActive(true);
        user.setIsEmailVerified(false);
        user.setIsPhoneVerified(false);
        user.setFailedLoginAttempts(0);

        user = userRepository.save(user);
        log.info("User registered successfully with ID: {}", user.getId());

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

        // Save refresh token
        saveRefreshToken(user.getId(), refreshToken);

        // Send verification OTP (in real app, send email/SMS)
        generateOtp(user.getEmail(), null, OtpType.EMAIL_VERIFICATION);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(jwtUtil.getAccessTokenExpiration())
                .refreshTokenExpiresIn(jwtUtil.getRefreshTokenExpiration())
                .isEmailVerified(user.getIsEmailVerified())
                .isPhoneVerified(user.getIsPhoneVerified())
                .build();
    }

    // Login User
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        log.info("Login attempt for: {}", request.getEmailOrPhone());

        // Find user by email or phone
        User user = userRepository.findByEmailOrPhone(request.getEmailOrPhone(), request.getEmailOrPhone())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", request.getEmailOrPhone());
                    return new InvalidCredentialsException("Invalid email/phone or password");
                });

        // Check if account is locked
        if (user.isAccountLocked()) {
            log.warn("Account locked for user: {}", user.getEmail());
            recordLoginHistory(user.getId(), ipAddress, userAgent, LoginStatus.BLOCKED, "Account locked");
            throw new AccountLockedException("Account is locked until " + user.getAccountLockedUntil());
        }

        // Check if account is active
        if (!user.getIsActive()) {
            log.warn("Inactive account login attempt: {}", user.getEmail());
            recordLoginHistory(user.getId(), ipAddress, userAgent, LoginStatus.BLOCKED, "Account inactive");
            throw new CustomException("Account is inactive. Please contact support.");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password for user: {}", user.getEmail());
            handleFailedLogin(user);
            recordLoginHistory(user.getId(), ipAddress, userAgent, LoginStatus.FAILED, "Invalid password");
            throw new InvalidCredentialsException("Invalid email/phone or password");
        }

        // Reset failed login attempts
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Record successful login
        recordLoginHistory(user.getId(), ipAddress, userAgent, LoginStatus.SUCCESS, null);

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

        // Save refresh token
        saveRefreshToken(user.getId(), refreshToken);

        log.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(jwtUtil.getAccessTokenExpiration())
                .refreshTokenExpiresIn(jwtUtil.getRefreshTokenExpiration())
                .isEmailVerified(user.getIsEmailVerified())
                .isPhoneVerified(user.getIsPhoneVerified())
                .build();
    }

    // Refresh Access Token
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refreshing access token");

        // Validate refresh token
        if (!jwtUtil.validateToken(request.getRefreshToken())) {
            throw new TokenExpiredException("Invalid or expired refresh token");
        }

        // Extract user info from token
        Long userId = jwtUtil.extractUserId(request.getRefreshToken());
        String email = jwtUtil.extractEmail(request.getRefreshToken());

        // Check if refresh token exists and is not revoked
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new TokenExpiredException("Refresh token not found"));

        if (refreshToken.getIsRevoked()) {
            throw new TokenExpiredException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            throw new TokenExpiredException("Refresh token has expired");
        }

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Generate new access token
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());

        log.info("Access token refreshed for user: {}", user.getEmail());

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .accessTokenExpiresIn(jwtUtil.getAccessTokenExpiration())
                .refreshTokenExpiresIn(jwtUtil.getRefreshTokenExpiration())
                .isEmailVerified(user.getIsEmailVerified())
                .isPhoneVerified(user.getIsPhoneVerified())
                .build();
    }

    // Logout User
    @Transactional
    public MessageResponse logout(String refreshToken) {
        log.info("Logging out user");

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

        // Revoke the refresh token
        token.setIsRevoked(true);
        refreshTokenRepository.save(token);

        log.info("User logged out successfully");
        return new MessageResponse("Logged out successfully");
    }

    // Change Password
    @Transactional
    public MessageResponse changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Changing password for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Old password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        // Revoke all refresh tokens (force re-login)
        refreshTokenRepository.findByUserId(userId).forEach(token -> {
            token.setIsRevoked(true);
            refreshTokenRepository.save(token);
        });

        log.info("Password changed successfully for user ID: {}", userId);
        return new MessageResponse("Password changed successfully. Please login again.");
    }

    // Forgot Password - Generate Reset Token
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        log.info("Password reset requested for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this email"));

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUserId(user.getId());
        passwordResetToken.setToken(resetToken);
        passwordResetToken.setExpiresAt(expiresAt);
        passwordResetToken.setIsUsed(false);

        passwordResetTokenRepository.save(passwordResetToken);

        // In real app, send email with reset link
        log.info("Password reset token generated: {}", resetToken);

        return new MessageResponse("Password reset link sent to your email");
    }

    // Reset Password using Token
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        log.info("Resetting password using token");

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new TokenExpiredException("Invalid or expired reset token"));

        if (resetToken.getIsUsed()) {
            throw new TokenExpiredException("Reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new TokenExpiredException("Reset token has expired");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        // Mark token as used
        resetToken.setIsUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Revoke all refresh tokens
        refreshTokenRepository.findByUserId(user.getId()).forEach(token -> {
            token.setIsRevoked(true);
            refreshTokenRepository.save(token);
        });

        log.info("Password reset successfully for user ID: {}", user.getId());
        return new MessageResponse("Password reset successfully. Please login with your new password.");
    }

    // Generate OTP
    @Transactional
    public MessageResponse generateOtp(String email, String phone, OtpType otpType) {
        log.info("Generating OTP for type: {}", otpType);

        // Generate 6-digit OTP
        String otpCode = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpirationMinutes);

        Otp otp = new Otp();
        otp.setEmail(email);
        otp.setPhone(phone);
        otp.setOtpCode(otpCode);
        otp.setOtpType(otpType);
        otp.setExpiresAt(expiresAt);
        otp.setIsUsed(false);

        otpRepository.save(otp);

        // In real app, send OTP via email/SMS
        log.info("OTP generated: {} (expires at: {})", otpCode, expiresAt);

        return new MessageResponse("OTP sent successfully. Valid for " + otpExpirationMinutes + " minutes.");
    }

    // Verify OTP
    @Transactional
    public MessageResponse verifyOtp(VerifyOtpRequest request) {
        log.info("Verifying OTP for type: {}", request.getOtpType());

        Otp otp;
        if (request.getEmailOrPhone().contains("@")) {
            otp = otpRepository.findByEmailAndOtpTypeAndIsUsed(
                    request.getEmailOrPhone(), request.getOtpType(), false
            ).orElseThrow(() -> new CustomException("Invalid or expired OTP"));
        } else {
            otp = otpRepository.findByPhoneAndOtpTypeAndIsUsed(
                    request.getEmailOrPhone(), request.getOtpType(), false
            ).orElseThrow(() -> new CustomException("Invalid or expired OTP"));
        }

        if (otp.isExpired()) {
            throw new CustomException("OTP has expired");
        }

        if (!otp.getOtpCode().equals(request.getOtpCode())) {
            throw new CustomException("Invalid OTP code");
        }

        // Mark OTP as used
        otp.setIsUsed(true);
        otpRepository.save(otp);

        // Update user verification status
        if (request.getOtpType() == OtpType.EMAIL_VERIFICATION && otp.getEmail() != null) {
            User user = userRepository.findByEmail(otp.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            user.setIsEmailVerified(true);
            userRepository.save(user);
        } else if (request.getOtpType() == OtpType.PHONE_VERIFICATION && otp.getPhone() != null) {
            User user = userRepository.findByPhone(otp.getPhone())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            user.setIsPhoneVerified(true);
            userRepository.save(user);
        }

        log.info("OTP verified successfully");
        return new MessageResponse("OTP verified successfully");
    }

    // Helper: Save Refresh Token
    private void saveRefreshToken(Long userId, String token) {
        LocalDateTime expiresAt = LocalDateTime.now().plusNanos(jwtUtil.getRefreshTokenExpiration() * 1_000_000);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(expiresAt);
        refreshToken.setIsRevoked(false);

        refreshTokenRepository.save(refreshToken);
    }

    // Helper: Handle Failed Login
    private void handleFailedLogin(User user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

        if (user.getFailedLoginAttempts() >= maxFailedLoginAttempts) {
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(lockDurationMinutes));
            log.warn("Account locked for user: {} until {}", user.getEmail(), user.getAccountLockedUntil());
        }

        userRepository.save(user);
    }

    // Helper: Record Login History
    private void recordLoginHistory(Long userId, String ipAddress, String userAgent, LoginStatus status, String failureReason) {
        LoginHistory history = new LoginHistory();
        history.setUserId(userId);
        history.setIpAddress(ipAddress);
        history.setUserAgent(userAgent);
        history.setLoginStatus(status);
        history.setFailureReason(failureReason);

        loginHistoryRepository.save(history);
    }
}
