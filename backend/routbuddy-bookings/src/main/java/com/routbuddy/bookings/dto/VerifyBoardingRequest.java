package com.routbuddy.bookings.dto;

import java.util.UUID;

public class VerifyBoardingRequest {

    private UUID tripId;
    private String qrToken;
    private String otpCode;

    public VerifyBoardingRequest() {}

    public VerifyBoardingRequest(UUID tripId, String qrToken, String otpCode) {
        this.tripId = tripId;
        this.qrToken = qrToken;
        this.otpCode = otpCode;
    }

    public UUID getTripId() { return tripId; }
    public void setTripId(UUID tripId) { this.tripId = tripId; }

    public String getQrToken() { return qrToken; }
    public void setQrToken(String qrToken) { this.qrToken = qrToken; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
}
