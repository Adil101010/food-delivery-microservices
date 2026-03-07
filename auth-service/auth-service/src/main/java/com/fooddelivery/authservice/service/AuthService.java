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
    private final RestaurantRepository restaurantRepository; // ✅ NEW

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

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("Email already registered");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new CustomException("Phone number already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setActive(true);
        user.setIsEmailVerified(false);
        user.setIsPhoneVerified(false);
        user.setFailedLoginAttempts(0);

        user = userRepository.save(user);
        log.info("User registered successfully with ID: {}", user.getId());

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

        saveRefreshToken(user.getId(), refreshToken);
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

        User user = userRepository.findByEmailOrPhone(request.getEmailOrPhone(), request.getEmailOrPhone())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", request.getEmailOrPhone());
                    return new InvalidCredentialsException("Invalid email/phone or password");
                });

        if (user.isAccountLocked()) {
            log.warn("Account locked for user: {}", user.getEmail());
            recordLoginHistory(user.getId(), ipAddress, userAgent, LoginStatus.BLOCKED, "Account locked");
            throw new AccountLockedException("Account is locked until " + user.getAccountLockedUntil());
        }

        if (!user.getActive()) {
            log.warn("Inactive account login attempt: {}", user.getEmail());
            recordLoginHistory(user.getId(), ipAddress, userAgent, LoginStatus.BLOCKED, "Account inactive");
            throw new CustomException("Account is inactive. Please contact support.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password for user: {}", user.getEmail());
            handleFailedLogin(user);
            recordLoginHistory(user.getId(), ipAddress, userAgent, LoginStatus.FAILED, "Invalid password");
            throw new InvalidCredentialsException("Invalid email/phone or password");
        }

        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        recordLoginHistory(user.getId(), ipAddress, userAgent, LoginStatus.SUCCESS, null);

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

        saveRefreshToken(user.getId(), refreshToken);

        // ✅ Restaurant info fetch karo
        Long restaurantId = null;
        String restaurantName = null;
        if (user.getRole().name().equals("RESTAURANT_OWNER")) {
            var restaurant = restaurantRepository.findByOwnerId(user.getId());
            if (restaurant.isPresent()) {
                restaurantId = restaurant.get().getId();
                restaurantName = restaurant.get().getName();
            }
        }

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
                .restaurantId(restaurantId)     // ✅ NEW
                .restaurantName(restaurantName) // ✅ NEW
                .build();
    }

    // Refresh Access Token
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refreshing access token");

        if (!jwtUtil.validateToken(request.getRefreshToken())) {
            throw new TokenExpiredException("Invalid or expired refresh token");
        }

        Long userId = jwtUtil.extractUserId(request.getRefreshToken());

        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new TokenExpiredException("Refresh token not found"));

        if (refreshToken.getIsRevoked()) {
            throw new TokenExpiredException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            throw new TokenExpiredException("Refresh token has expired");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

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

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        refreshTokenRepository.findByUserId(userId).forEach(token -> {
            token.setIsRevoked(true);
            refreshTokenRepository.save(token);
        });

        log.info("Password changed successfully for user ID: {}", userId);
        return new MessageResponse("Password changed successfully. Please login again.");
    }

    // Forgot Password
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        log.info("Password reset requested for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this email"));

        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUserId(user.getId());
        passwordResetToken.setToken(resetToken);
        passwordResetToken.setExpiresAt(expiresAt);
        passwordResetToken.setIsUsed(false);

        passwordResetTokenRepository.save(passwordResetToken);

        log.info("Password reset token generated: {}", resetToken);
        return new MessageResponse("Password reset link sent to your email");
    }

    // Reset Password
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

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        resetToken.setIsUsed(true);
        passwordResetTokenRepository.save(resetToken);

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

        otp.setIsUsed(true);
        otpRepository.save(otp);

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
