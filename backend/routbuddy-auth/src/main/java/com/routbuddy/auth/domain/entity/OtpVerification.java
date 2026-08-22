package com.routbuddy.auth.domain.entity;

import com.routbuddy.common.domain.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "otp_verifications", indexes = {
        @Index(name = "idx_otp_phone", columnList = "phone")
})
public class OtpVerification extends BaseEntity {

    @Column(name = "phone", nullable = false, length = 15)
    private String phone;

    @Column(name = "otp_code", nullable = false, length = 10)
    private String otpCode;

    @Column(name = "expiry_time", nullable = false)
    private Instant expiryTime;

    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;

    @Column(name = "attempts_count", nullable = false)
    private int attemptsCount = 0;

    public OtpVerification() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String phone;
        private String otpCode;
        private Instant expiryTime;
        private boolean verified = false;
        private int attemptsCount = 0;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder otpCode(String otpCode) { this.otpCode = otpCode; return this; }
        public Builder expiryTime(Instant expiryTime) { this.expiryTime = expiryTime; return this; }
        public Builder verified(boolean verified) { this.verified = verified; return this; }
        public Builder attemptsCount(int attemptsCount) { this.attemptsCount = attemptsCount; return this; }

        public OtpVerification build() {
            OtpVerification ov = new OtpVerification();
            if (id != null) ov.setId(id);
            ov.setPhone(phone);
            ov.setOtpCode(otpCode);
            ov.setExpiryTime(expiryTime);
            ov.setVerified(verified);
            ov.setAttemptsCount(attemptsCount);
            return ov;
        }
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryTime);
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public Instant getExpiryTime() { return expiryTime; }
    public void setExpiryTime(Instant expiryTime) { this.expiryTime = expiryTime; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public int getAttemptsCount() { return attemptsCount; }
    public void setAttemptsCount(int attemptsCount) { this.attemptsCount = attemptsCount; }
}
