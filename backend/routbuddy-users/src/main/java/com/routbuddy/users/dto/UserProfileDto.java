package com.routbuddy.users.dto;

import com.routbuddy.common.domain.enums.UserRole;
import com.routbuddy.users.domain.entity.User;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserProfileDto {
    private UUID id;
    private String phone;
    private String email;
    private String fullName;
    private UserRole primaryRole;
    private Set<String> roles;
    private String gender;
    private String profileImageUrl;
    private boolean isVerified;
    private double trustScore;
    private String orgName;
    private String orgEmail;
    private boolean isOrgVerified;
    private String languagePreference;
    private Instant createdAt;

    public UserProfileDto() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String phone;
        private String email;
        private String fullName;
        private UserRole primaryRole;
        private Set<String> roles;
        private String gender;
        private String profileImageUrl;
        private boolean isVerified;
        private double trustScore;
        private String orgName;
        private String orgEmail;
        private boolean isOrgVerified;
        private String languagePreference;
        private Instant createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder fullName(String fullName) { this.fullName = fullName; return this; }
        public Builder primaryRole(UserRole primaryRole) { this.primaryRole = primaryRole; return this; }
        public Builder roles(Set<String> roles) { this.roles = roles; return this; }
        public Builder gender(String gender) { this.gender = gender; return this; }
        public Builder profileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; return this; }
        public Builder isVerified(boolean isVerified) { this.isVerified = isVerified; return this; }
        public Builder trustScore(double trustScore) { this.trustScore = trustScore; return this; }
        public Builder orgName(String orgName) { this.orgName = orgName; return this; }
        public Builder orgEmail(String orgEmail) { this.orgEmail = orgEmail; return this; }
        public Builder isOrgVerified(boolean isOrgVerified) { this.isOrgVerified = isOrgVerified; return this; }
        public Builder languagePreference(String languagePreference) { this.languagePreference = languagePreference; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public UserProfileDto build() {
            UserProfileDto dto = new UserProfileDto();
            dto.id = id;
            dto.phone = phone;
            dto.email = email;
            dto.fullName = fullName;
            dto.primaryRole = primaryRole;
            dto.roles = roles;
            dto.gender = gender;
            dto.profileImageUrl = profileImageUrl;
            dto.isVerified = isVerified;
            dto.trustScore = trustScore;
            dto.orgName = orgName;
            dto.orgEmail = orgEmail;
            dto.isOrgVerified = isOrgVerified;
            dto.languagePreference = languagePreference;
            dto.createdAt = createdAt;
            return dto;
        }
    }

    public static UserProfileDto fromEntity(User user) {
        Set<String> roleNames = user.getRoles() != null
                ? user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet())
                : Set.of();

        return UserProfileDto.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .primaryRole(user.getPrimaryRole())
                .roles(roleNames)
                .gender(user.getGender())
                .profileImageUrl(user.getProfileImageUrl())
                .isVerified(user.isVerified())
                .trustScore(user.getTrustScore())
                .orgName(user.getOrgName())
                .orgEmail(user.getOrgEmail())
                .isOrgVerified(user.isOrgVerified())
                .languagePreference(user.getLanguagePreference())
                .createdAt(user.getCreatedAt())
                .build();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
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
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean isVerified) { this.isVerified = isVerified; }
    public double getTrustScore() { return trustScore; }
    public void setTrustScore(double trustScore) { this.trustScore = trustScore; }
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public String getOrgEmail() { return orgEmail; }
    public void setOrgEmail(String orgEmail) { this.orgEmail = orgEmail; }
    public boolean isOrgVerified() { return isOrgVerified; }
    public void setOrgVerified(boolean isOrgVerified) { this.isOrgVerified = isOrgVerified; }
    public String getLanguagePreference() { return languagePreference; }
    public void setLanguagePreference(String languagePreference) { this.languagePreference = languagePreference; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
