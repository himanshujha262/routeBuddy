package com.routbuddy.auth.dto;

public class TokenRefreshResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresInMs;

    public TokenRefreshResponse() {}

    public TokenRefreshResponse(String accessToken, String refreshToken, String tokenType, long expiresInMs) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType != null ? tokenType : "Bearer";
        this.expiresInMs = expiresInMs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";
        private long expiresInMs;

        public Builder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public Builder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }
        public Builder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
        public Builder expiresInMs(long expiresInMs) { this.expiresInMs = expiresInMs; return this; }

        public TokenRefreshResponse build() {
            return new TokenRefreshResponse(accessToken, refreshToken, tokenType, expiresInMs);
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
}
