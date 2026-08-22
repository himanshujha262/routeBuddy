package com.routbuddy.drivers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class RegisterDriverRequest {

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    @NotNull(message = "License expiry date is required")
    private LocalDate licenseExpiryDate;

    private String licenseFrontImageUrl;
    private String licenseBackImageUrl;
    private String aadhaarMasked;

    public RegisterDriverRequest() {}

    public RegisterDriverRequest(String licenseNumber, LocalDate licenseExpiryDate, String licenseFrontImageUrl, String licenseBackImageUrl, String aadhaarMasked) {
        this.licenseNumber = licenseNumber;
        this.licenseExpiryDate = licenseExpiryDate;
        this.licenseFrontImageUrl = licenseFrontImageUrl;
        this.licenseBackImageUrl = licenseBackImageUrl;
        this.aadhaarMasked = aadhaarMasked;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String licenseNumber;
        private LocalDate licenseExpiryDate;
        private String licenseFrontImageUrl;
        private String licenseBackImageUrl;
        private String aadhaarMasked;

        public Builder licenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; return this; }
        public Builder licenseExpiryDate(LocalDate licenseExpiryDate) { this.licenseExpiryDate = licenseExpiryDate; return this; }
        public Builder licenseFrontImageUrl(String licenseFrontImageUrl) { this.licenseFrontImageUrl = licenseFrontImageUrl; return this; }
        public Builder licenseBackImageUrl(String licenseBackImageUrl) { this.licenseBackImageUrl = licenseBackImageUrl; return this; }
        public Builder aadhaarMasked(String aadhaarMasked) { this.aadhaarMasked = aadhaarMasked; return this; }

        public RegisterDriverRequest build() {
            return new RegisterDriverRequest(licenseNumber, licenseExpiryDate, licenseFrontImageUrl, licenseBackImageUrl, aadhaarMasked);
        }
    }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public LocalDate getLicenseExpiryDate() { return licenseExpiryDate; }
    public void setLicenseExpiryDate(LocalDate licenseExpiryDate) { this.licenseExpiryDate = licenseExpiryDate; }

    public String getLicenseFrontImageUrl() { return licenseFrontImageUrl; }
    public void setLicenseFrontImageUrl(String licenseFrontImageUrl) { this.licenseFrontImageUrl = licenseFrontImageUrl; }

    public String getLicenseBackImageUrl() { return licenseBackImageUrl; }
    public void setLicenseBackImageUrl(String licenseBackImageUrl) { this.licenseBackImageUrl = licenseBackImageUrl; }

    public String getAadhaarMasked() { return aadhaarMasked; }
    public void setAadhaarMasked(String aadhaarMasked) { this.aadhaarMasked = aadhaarMasked; }
}
