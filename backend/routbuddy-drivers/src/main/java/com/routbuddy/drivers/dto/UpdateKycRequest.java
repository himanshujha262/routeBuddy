package com.routbuddy.drivers.dto;

import com.routbuddy.common.domain.enums.KYCStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateKycRequest {

    @NotNull(message = "KYC status is required")
    private KYCStatus status;

    private String rejectionReason;

    public UpdateKycRequest() {}

    public UpdateKycRequest(KYCStatus status, String rejectionReason) {
        this.status = status;
        this.rejectionReason = rejectionReason;
    }

    public KYCStatus getStatus() {
        return status;
    }

    public void setStatus(KYCStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
