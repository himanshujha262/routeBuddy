package com.routbuddy.auth.dto;

import com.routbuddy.common.domain.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number format (10 digits starting with 6-9)")
    private String phone;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters long")
    private String password;

    @NotNull(message = "Primary role is required")
    private UserRole role;

    private String gender;
    private String orgName;
    private String orgEmail;

    public RegisterRequest() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String phone;
        private String fullName;
        private String email;
        private String password;
        private UserRole role;
        private String gender;
        private String orgName;
        private String orgEmail;

        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder fullName(String fullName) { this.fullName = fullName; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder role(UserRole role) { this.role = role; return this; }
        public Builder gender(String gender) { this.gender = gender; return this; }
        public Builder orgName(String orgName) { this.orgName = orgName; return this; }
        public Builder orgEmail(String orgEmail) { this.orgEmail = orgEmail; return this; }

        public RegisterRequest build() {
            RegisterRequest req = new RegisterRequest();
            req.phone = phone;
            req.fullName = fullName;
            req.email = email;
            req.password = password;
            req.role = role;
            req.gender = gender;
            req.orgName = orgName;
            req.orgEmail = orgEmail;
            return req;
        }
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public String getOrgEmail() { return orgEmail; }
    public void setOrgEmail(String orgEmail) { this.orgEmail = orgEmail; }
}
