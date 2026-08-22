package com.routbuddy.drivers.dto;

import com.routbuddy.common.domain.enums.KYCStatus;
import com.routbuddy.drivers.domain.entity.DriverProfile;

import java.time.LocalDate;
import java.util.UUID;

public class DriverProfileDto {
    private UUID id;
    private UUID userId;
    private String licenseNumber;
    private LocalDate licenseExpiryDate;
    private String licenseFrontImageUrl;
    private String licenseBackImageUrl;
    private String aadhaarMasked;
    private KYCStatus kycStatus;
    private String kycRejectionReason;
    private boolean policeVerified;
    private double ratingAvg;
    private int totalRatingsCount;
    private int totalTripsCompleted;
    private double totalEarningsInr;
    private double walletBalanceInr;
    private boolean online;
    private UUID currentVehicleId;
    private UUID activeRouteId;

    public DriverProfileDto() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID userId;
        private String licenseNumber;
        private LocalDate licenseExpiryDate;
        private String licenseFrontImageUrl;
        private String licenseBackImageUrl;
        private String aadhaarMasked;
        private KYCStatus kycStatus;
        private String kycRejectionReason;
        private boolean policeVerified;
        private double ratingAvg;
        private int totalRatingsCount;
        private int totalTripsCompleted;
        private double totalEarningsInr;
        private double walletBalanceInr;
        private boolean online;
        private UUID currentVehicleId;
        private UUID activeRouteId;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder userId(UUID userId) { this.userId = userId; return this; }
        public Builder licenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; return this; }
        public Builder licenseExpiryDate(LocalDate licenseExpiryDate) { this.licenseExpiryDate = licenseExpiryDate; return this; }
        public Builder licenseFrontImageUrl(String licenseFrontImageUrl) { this.licenseFrontImageUrl = licenseFrontImageUrl; return this; }
        public Builder licenseBackImageUrl(String licenseBackImageUrl) { this.licenseBackImageUrl = licenseBackImageUrl; return this; }
        public Builder aadhaarMasked(String aadhaarMasked) { this.aadhaarMasked = aadhaarMasked; return this; }
        public Builder kycStatus(KYCStatus kycStatus) { this.kycStatus = kycStatus; return this; }
        public Builder kycRejectionReason(String kycRejectionReason) { this.kycRejectionReason = kycRejectionReason; return this; }
        public Builder policeVerified(boolean policeVerified) { this.policeVerified = policeVerified; return this; }
        public Builder ratingAvg(double ratingAvg) { this.ratingAvg = ratingAvg; return this; }
        public Builder totalRatingsCount(int totalRatingsCount) { this.totalRatingsCount = totalRatingsCount; return this; }
        public Builder totalTripsCompleted(int totalTripsCompleted) { this.totalTripsCompleted = totalTripsCompleted; return this; }
        public Builder totalEarningsInr(double totalEarningsInr) { this.totalEarningsInr = totalEarningsInr; return this; }
        public Builder walletBalanceInr(double walletBalanceInr) { this.walletBalanceInr = walletBalanceInr; return this; }
        public Builder online(boolean online) { this.online = online; return this; }
        public Builder currentVehicleId(UUID currentVehicleId) { this.currentVehicleId = currentVehicleId; return this; }
        public Builder activeRouteId(UUID activeRouteId) { this.activeRouteId = activeRouteId; return this; }

        public DriverProfileDto build() {
            DriverProfileDto dto = new DriverProfileDto();
            dto.id = id;
            dto.userId = userId;
            dto.licenseNumber = licenseNumber;
            dto.licenseExpiryDate = licenseExpiryDate;
            dto.licenseFrontImageUrl = licenseFrontImageUrl;
            dto.licenseBackImageUrl = licenseBackImageUrl;
            dto.aadhaarMasked = aadhaarMasked;
            dto.kycStatus = kycStatus;
            dto.kycRejectionReason = kycRejectionReason;
            dto.policeVerified = policeVerified;
            dto.ratingAvg = ratingAvg;
            dto.totalRatingsCount = totalRatingsCount;
            dto.totalTripsCompleted = totalTripsCompleted;
            dto.totalEarningsInr = totalEarningsInr;
            dto.walletBalanceInr = walletBalanceInr;
            dto.online = online;
            dto.currentVehicleId = currentVehicleId;
            dto.activeRouteId = activeRouteId;
            return dto;
        }
    }

    public static DriverProfileDto fromEntity(DriverProfile profile) {
        return DriverProfileDto.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .licenseNumber(profile.getLicenseNumber())
                .licenseExpiryDate(profile.getLicenseExpiryDate())
                .licenseFrontImageUrl(profile.getLicenseFrontImageUrl())
                .licenseBackImageUrl(profile.getLicenseBackImageUrl())
                .aadhaarMasked(profile.getAadhaarMasked())
                .kycStatus(profile.getKycStatus())
                .kycRejectionReason(profile.getKycRejectionReason())
                .policeVerified(profile.isPoliceVerified())
                .ratingAvg(profile.getRatingAvg())
                .totalRatingsCount(profile.getTotalRatingsCount())
                .totalTripsCompleted(profile.getTotalTripsCompleted())
                .totalEarningsInr(profile.getTotalEarningsInr())
                .walletBalanceInr(profile.getWalletBalanceInr())
                .online(profile.isOnline())
                .currentVehicleId(profile.getCurrentVehicleId())
                .activeRouteId(profile.getActiveRouteId())
                .build();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
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
    public KYCStatus getKycStatus() { return kycStatus; }
    public void setKycStatus(KYCStatus kycStatus) { this.kycStatus = kycStatus; }
    public String getKycRejectionReason() { return kycRejectionReason; }
    public void setKycRejectionReason(String kycRejectionReason) { this.kycRejectionReason = kycRejectionReason; }
    public boolean isPoliceVerified() { return policeVerified; }
    public void setPoliceVerified(boolean policeVerified) { this.policeVerified = policeVerified; }
    public double getRatingAvg() { return ratingAvg; }
    public void setRatingAvg(double ratingAvg) { this.ratingAvg = ratingAvg; }
    public int getTotalRatingsCount() { return totalRatingsCount; }
    public void setTotalRatingsCount(int totalRatingsCount) { this.totalRatingsCount = totalRatingsCount; }
    public int getTotalTripsCompleted() { return totalTripsCompleted; }
    public void setTotalTripsCompleted(int totalTripsCompleted) { this.totalTripsCompleted = totalTripsCompleted; }
    public double getTotalEarningsInr() { return totalEarningsInr; }
    public void setTotalEarningsInr(double totalEarningsInr) { this.totalEarningsInr = totalEarningsInr; }
    public double getWalletBalanceInr() { return walletBalanceInr; }
    public void setWalletBalanceInr(double walletBalanceInr) { this.walletBalanceInr = walletBalanceInr; }
    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }
    public UUID getCurrentVehicleId() { return currentVehicleId; }
    public void setCurrentVehicleId(UUID currentVehicleId) { this.currentVehicleId = currentVehicleId; }
    public UUID getActiveRouteId() { return activeRouteId; }
    public void setActiveRouteId(UUID activeRouteId) { this.activeRouteId = activeRouteId; }
}
