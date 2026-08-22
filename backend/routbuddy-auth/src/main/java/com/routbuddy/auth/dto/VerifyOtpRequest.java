package com.routbuddy.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class VerifyOtpRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number format")
    private String phone;

    @NotBlank(message = "OTP code is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits")
    private String otpCode;

    public VerifyOtpRequest() {}

    public VerifyOtpRequest(String phone, String otpCode) {
        this.phone = phone;
        this.otpCode = otpCode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String phone;
        private String otpCode;

        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder otpCode(String otpCode) { this.otpCode = otpCode; return this; }

        public VerifyOtpRequest build() {
            return new VerifyOtpRequest(phone, otpCode);
        }
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
}
