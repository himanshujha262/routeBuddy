package com.routbuddy.auth.dto;

import com.routbuddy.common.domain.enums.UserRole;

import java.util.Set;
import java.util.UUID;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresInMs;
    private UUID userId;
    private String phone;
    private String email;
    private String fullName;
    private UserRole primaryRole;
    private Set<String> roles;
    private boolean isVerified;
    private double trustScore;

    public AuthResponse() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";
        private long expiresInMs;
        private UUID userId;
        private String phone;
        private String email;
        private String fullName;
        private UserRole primaryRole;
        private Set<String> roles;
        private boolean isVerified;
        private double trustScore;

        public Builder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public Builder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }
        public Builder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
        public Builder expiresInMs(long expiresInMs) { this.expiresInMs = expiresInMs; return this; }
        public Builder userId(UUID userId) { this.userId = userId; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder fullName(String fullName) { this.fullName = fullName; return this; }
        public Builder primaryRole(UserRole primaryRole) { this.primaryRole = primaryRole; return this; }
        public Builder roles(Set<String> roles) { this.roles = roles; return this; }
        public Builder isVerified(boolean isVerified) { this.isVerified = isVerified; return this; }
        public Builder trustScore(double trustScore) { this.trustScore = trustScore; return this; }

        public AuthResponse build() {
            AuthResponse resp = new AuthResponse();
            resp.accessToken = accessToken;
            resp.refreshToken = refreshToken;
            resp.tokenType = tokenType != null ? tokenType : "Bearer";
            resp.expiresInMs = expiresInMs;
            resp.userId = userId;
            resp.phone = phone;
            resp.email = email;
            resp.fullName = fullName;
            resp.primaryRole = primaryRole;
            resp.roles = roles;
            resp.isVerified = isVerified;
            resp.trustScore = trustScore;
            return resp;
        }
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public long getExpiresInMs() { return expiresInMs; }
    public void setExpiresInMs(long expiresInMs) { this.expiresInMs = expiresInMs; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public UserRole getPrimaryRole() { return primaryRole; }
    public void setPrimaryRole(UserRole primaryRole) { this.primaryRole = primaryRole; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean isVerified) { this.isVerified = isVerified; }

    public double getTrustScore() { return trustScore; }
    public void setTrustScore(double trustScore) { this.trustScore = trustScore; }
}
