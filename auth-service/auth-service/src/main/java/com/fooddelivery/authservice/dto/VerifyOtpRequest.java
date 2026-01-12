package com.fooddelivery.authservice.dto;

import com.fooddelivery.authservice.enums.OtpType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {

    @NotBlank(message = "Email or phone is required")
    private String emailOrPhone;

    @NotBlank(message = "OTP code is required")
    private String otpCode;

    @NotNull(message = "OTP type is required")
    private OtpType otpType;
}
