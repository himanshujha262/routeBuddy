package com.routbuddy.drivers.repository;

import com.routbuddy.common.domain.enums.KYCStatus;
import com.routbuddy.drivers.domain.entity.DriverProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<DriverProfile, UUID> {
    Optional<DriverProfile> findByUserId(UUID userId);
    Optional<DriverProfile> findByLicenseNumber(String licenseNumber);
    Page<DriverProfile> findByKycStatus(KYCStatus kycStatus, Pageable pageable);
    long countByKycStatus(KYCStatus kycStatus);
    long countByOnlineTrue();
}
