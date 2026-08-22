package com.routbuddy.drivers.domain.entity;

import com.routbuddy.common.domain.entity.BaseEntity;
import com.routbuddy.common.domain.enums.KYCStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "driver_profiles", indexes = {
        @Index(name = "idx_drivers_user_id", columnList = "user_id", unique = true),
        @Index(name = "idx_drivers_kyc_status", columnList = "kyc_status"),
        @Index(name = "idx_drivers_is_online", columnList = "is_online")
})
public class DriverProfile extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "license_number", nullable = false, unique = true, length = 30)
    private String licenseNumber;

    @Column(name = "license_expiry_date")
    private LocalDate licenseExpiryDate;

    @Column(name = "license_front_image_url")
    private String licenseFrontImageUrl;

    @Column(name = "license_back_image_url")
    private String licenseBackImageUrl;

    @Column(name = "aadhaar_masked", length = 20)
    private String aadhaarMasked;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false, length = 20)
    private KYCStatus kycStatus = KYCStatus.PENDING;

    @Column(name = "kyc_rejection_reason")
    private String kycRejectionReason;

    @Column(name = "is_police_verified", nullable = false)
    private boolean policeVerified = false;

    @Column(name = "rating_avg", nullable = false)
    private double ratingAvg = 5.0;

    @Column(name = "total_ratings_count", nullable = false)
    private int totalRatingsCount = 0;

    @Column(name = "total_trips_completed", nullable = false)
    private int totalTripsCompleted = 0;

    @Column(name = "total_earnings_inr", nullable = false)
    private double totalEarningsInr = 0.0;

    @Column(name = "wallet_balance_inr", nullable = false)
    private double walletBalanceInr = 0.0;

    @Column(name = "is_online", nullable = false)
    private boolean online = false;

    @Column(name = "current_vehicle_id")
    private UUID currentVehicleId;

    @Column(name = "active_route_id")
    private UUID activeRouteId;

    public DriverProfile() {}

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
        private KYCStatus kycStatus = KYCStatus.PENDING;
        private String kycRejectionReason;
        private boolean policeVerified = false;
        private double ratingAvg = 5.0;
        private int totalRatingsCount = 0;
        private int totalTripsCompleted = 0;
        private double totalEarningsInr = 0.0;
        private double walletBalanceInr = 0.0;
        private boolean online = false;
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

        public DriverProfile build() {
            DriverProfile dp = new DriverProfile();
            if (id != null) dp.setId(id);
            dp.setUserId(userId);
            dp.setLicenseNumber(licenseNumber);
            dp.setLicenseExpiryDate(licenseExpiryDate);
            dp.setLicenseFrontImageUrl(licenseFrontImageUrl);
            dp.setLicenseBackImageUrl(licenseBackImageUrl);
            dp.setAadhaarMasked(aadhaarMasked);
            dp.setKycStatus(kycStatus != null ? kycStatus : KYCStatus.PENDING);
            dp.setKycRejectionReason(kycRejectionReason);
            dp.setPoliceVerified(policeVerified);
            dp.setRatingAvg(ratingAvg);
            dp.setTotalRatingsCount(totalRatingsCount);
            dp.setTotalTripsCompleted(totalTripsCompleted);
            dp.setTotalEarningsInr(totalEarningsInr);
            dp.setWalletBalanceInr(walletBalanceInr);
            dp.setOnline(online);
            dp.setCurrentVehicleId(currentVehicleId);
            dp.setActiveRouteId(activeRouteId);
            return dp;
        }
    }

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
