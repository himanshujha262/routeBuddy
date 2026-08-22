package com.routbuddy.users.domain.entity;

import com.routbuddy.common.domain.entity.BaseEntity;
import com.routbuddy.common.domain.enums.UserRole;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_phone", columnList = "phone", unique = true),
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_primary_role", columnList = "primary_role")
})
public class User extends BaseEntity {

    @Column(name = "phone", nullable = false, unique = true, length = 15)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_role", nullable = false, length = 30)
    private UserRole primaryRole = UserRole.PASSENGER;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;

    @Column(name = "trust_score", nullable = false)
    private double trustScore = 4.5;

    @Column(name = "org_name", length = 150)
    private String orgName;

    @Column(name = "org_email", length = 100)
    private String orgEmail;

    @Column(name = "is_org_verified", nullable = false)
    private boolean orgVerified = false;

    @Column(name = "language_preference", length = 30)
    private String languagePreference = "English";

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmergencyContact> emergencyContacts = new ArrayList<>();

    public User() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String phone;
        private String email;
        private String passwordHash;
        private String fullName;
        private UserRole primaryRole = UserRole.PASSENGER;
        private Set<Role> roles = new HashSet<>();
        private String gender;
        private String profileImageUrl;
        private boolean verified = false;
        private double trustScore = 4.5;
        private String orgName;
        private String orgEmail;
        private boolean orgVerified = false;
        private String languagePreference = "English";
        private boolean active = true;
        private Instant lastLoginAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder primaryRole(UserRole primaryRole) {
            this.primaryRole = primaryRole;
            return this;
        }

        public Builder roles(Set<Role> roles) {
            this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
            return this;
        }

        public Builder gender(String gender) {
            this.gender = gender;
            return this;
        }

        public Builder profileImageUrl(String profileImageUrl) {
            this.profileImageUrl = profileImageUrl;
            return this;
        }

        public Builder verified(boolean verified) {
            this.verified = verified;
            return this;
        }

        public Builder trustScore(double trustScore) {
            this.trustScore = trustScore;
            return this;
        }

        public Builder orgName(String orgName) {
            this.orgName = orgName;
            return this;
        }

        public Builder orgEmail(String orgEmail) {
            this.orgEmail = orgEmail;
            return this;
        }

        public Builder orgVerified(boolean orgVerified) {
            this.orgVerified = orgVerified;
            return this;
        }

        public Builder languagePreference(String languagePreference) {
            this.languagePreference = languagePreference;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder lastLoginAt(Instant lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
            return this;
        }

        public User build() {
            User user = new User();
            if (id != null) user.setId(id);
            user.setPhone(phone);
            user.setEmail(email);
            user.setPasswordHash(passwordHash);
            user.setFullName(fullName);
            user.setPrimaryRole(primaryRole != null ? primaryRole : UserRole.PASSENGER);
            user.setRoles(roles != null ? roles : new HashSet<>());
            user.setGender(gender);
            user.setProfileImageUrl(profileImageUrl);
            user.setVerified(verified);
            user.setTrustScore(trustScore);
            user.setOrgName(orgName);
            user.setOrgEmail(orgEmail);
            user.setOrgVerified(orgVerified);
            user.setLanguagePreference(languagePreference != null ? languagePreference : "English");
            user.setActive(active);
            user.setLastLoginAt(lastLoginAt);
            return user;
        }
    }

    public void addRole(Role role) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        if (this.roles != null) {
            this.roles.remove(role);
        }
    }

    public boolean hasRole(UserRole roleName) {
        return this.roles != null && this.roles.stream().anyMatch(r -> r.getName() == roleName);
    }

    // Getters and Setters
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public UserRole getPrimaryRole() { return primaryRole; }
    public void setPrimaryRole(UserRole primaryRole) { this.primaryRole = primaryRole; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public double getTrustScore() { return trustScore; }
    public void setTrustScore(double trustScore) { this.trustScore = trustScore; }

    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }

    public String getOrgEmail() { return orgEmail; }
    public void setOrgEmail(String orgEmail) { this.orgEmail = orgEmail; }

    public boolean isOrgVerified() { return orgVerified; }
    public void setOrgVerified(boolean orgVerified) { this.orgVerified = orgVerified; }

    public String getLanguagePreference() { return languagePreference; }
    public void setLanguagePreference(String languagePreference) { this.languagePreference = languagePreference; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public List<EmergencyContact> getEmergencyContacts() { return emergencyContacts; }
    public void setEmergencyContacts(List<EmergencyContact> emergencyContacts) { this.emergencyContacts = emergencyContacts; }
}
