package com.routbuddy.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Identifier (phone or email) is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequest() {}

    public LoginRequest(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String identifier;
        private String password;

        public Builder identifier(String identifier) { this.identifier = identifier; return this; }
        public Builder password(String password) { this.password = password; return this; }

        public LoginRequest build() {
            return new LoginRequest(identifier, password);
        }
    }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
