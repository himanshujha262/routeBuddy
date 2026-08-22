package com.routbuddy.users.dto;

public class UpdateProfileRequest {
    private String fullName;
    private String email;
    private String gender;
    private String profileImageUrl;
    private String orgName;
    private String orgEmail;
    private String languagePreference;

    public UpdateProfileRequest() {}

    public UpdateProfileRequest(String fullName, String email, String gender, String profileImageUrl, String orgName, String orgEmail, String languagePreference) {
        this.fullName = fullName;
        this.email = email;
        this.gender = gender;
        this.profileImageUrl = profileImageUrl;
        this.orgName = orgName;
        this.orgEmail = orgEmail;
        this.languagePreference = languagePreference;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String fullName;
        private String email;
        private String gender;
        private String profileImageUrl;
        private String orgName;
        private String orgEmail;
        private String languagePreference;

        public Builder fullName(String fullName) { this.fullName = fullName; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder gender(String gender) { this.gender = gender; return this; }
        public Builder profileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; return this; }
        public Builder orgName(String orgName) { this.orgName = orgName; return this; }
        public Builder orgEmail(String orgEmail) { this.orgEmail = orgEmail; return this; }
        public Builder languagePreference(String languagePreference) { this.languagePreference = languagePreference; return this; }

        public UpdateProfileRequest build() {
            return new UpdateProfileRequest(fullName, email, gender, profileImageUrl, orgName, orgEmail, languagePreference);
        }
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public String getOrgEmail() { return orgEmail; }
    public void setOrgEmail(String orgEmail) { this.orgEmail = orgEmail; }
    public String getLanguagePreference() { return languagePreference; }
    public void setLanguagePreference(String languagePreference) { this.languagePreference = languagePreference; }
}
