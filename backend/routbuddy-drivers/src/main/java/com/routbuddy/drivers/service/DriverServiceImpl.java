package com.routbuddy.drivers.service;

import com.routbuddy.common.domain.enums.KYCStatus;
import com.routbuddy.common.domain.enums.UserRole;
import com.routbuddy.common.exception.BadRequestException;
import com.routbuddy.common.exception.ResourceNotFoundException;
import com.routbuddy.common.exception.UserAlreadyExistsException;
import com.routbuddy.drivers.domain.entity.DriverProfile;
import com.routbuddy.drivers.dto.DriverProfileDto;
import com.routbuddy.drivers.dto.RegisterDriverRequest;
import com.routbuddy.drivers.dto.UpdateKycRequest;
import com.routbuddy.drivers.repository.DriverRepository;
import com.routbuddy.users.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DriverServiceImpl implements DriverService {

    private static final Logger log = LoggerFactory.getLogger(DriverServiceImpl.class);

    private final DriverRepository driverRepository;
    private final UserService userService;

    public DriverServiceImpl(DriverRepository driverRepository, UserService userService) {
        this.driverRepository = driverRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public DriverProfileDto registerDriver(UUID userId, RegisterDriverRequest request) {
        if (driverRepository.findByUserId(userId).isPresent()) {
            throw new UserAlreadyExistsException("Driver profile already exists for this user account");
        }

        if (driverRepository.findByLicenseNumber(request.getLicenseNumber()).isPresent()) {
            throw new UserAlreadyExistsException("A driver with license number " + request.getLicenseNumber() + " already exists");
        }

        userService.assignRoleToUser(userId, UserRole.DRIVER);

        DriverProfile profile = DriverProfile.builder()
                .userId(userId)
                .licenseNumber(request.getLicenseNumber().trim().toUpperCase())
                .licenseExpiryDate(request.getLicenseExpiryDate())
                .licenseFrontImageUrl(request.getLicenseFrontImageUrl())
                .licenseBackImageUrl(request.getLicenseBackImageUrl())
                .aadhaarMasked(request.getAadhaarMasked())
                .kycStatus(KYCStatus.PENDING)
                .ratingAvg(5.0)
                .totalRatingsCount(0)
                .totalTripsCompleted(0)
                .totalEarningsInr(0.0)
                .walletBalanceInr(0.0)
                .online(false)
                .build();

        DriverProfile saved = driverRepository.save(profile);
        log.info("Registered driver profile for user id: {}, driver id: {}", userId, saved.getId());
        return DriverProfileDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DriverProfileDto getDriverProfileByUserId(UUID userId) {
        DriverProfile profile = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("DriverProfile", "userId", userId));
        return DriverProfileDto.fromEntity(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public DriverProfileDto getDriverProfile(UUID driverId) {
        DriverProfile profile = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("DriverProfile", "id", driverId));
        return DriverProfileDto.fromEntity(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public DriverProfile getDriverEntity(UUID driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("DriverProfile", "id", driverId));
    }

    @Override
    @Transactional(readOnly = true)
    public DriverProfile getDriverEntityByUserId(UUID userId) {
        return driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("DriverProfile", "userId", userId));
    }

    @Override
    @Transactional
    public DriverProfileDto updateOnlineStatus(UUID userId, boolean online) {
        DriverProfile profile = getDriverEntityByUserId(userId);

        if (online && profile.getKycStatus() != KYCStatus.APPROVED) {
            throw new BadRequestException("Driver KYC must be APPROVED before going online. Current status: " + profile.getKycStatus());
        }

        profile.setOnline(online);
        DriverProfile updated = driverRepository.save(profile);
        log.info("Updated driver {} online status to: {}", profile.getId(), online);
        return DriverProfileDto.fromEntity(updated);
    }

    @Override
    @Transactional
    public DriverProfileDto updateKycStatus(UUID driverId, UpdateKycRequest request) {
        DriverProfile profile = getDriverEntity(driverId);
        profile.setKycStatus(request.getStatus());
        if (request.getStatus() == KYCStatus.REJECTED) {
            profile.setKycRejectionReason(request.getRejectionReason());
            profile.setOnline(false);
        } else if (request.getStatus() == KYCStatus.APPROVED) {
            profile.setKycRejectionReason(null);
        }

        DriverProfile updated = driverRepository.save(profile);
        log.info("Admin updated driver {} KYC status to: {}", driverId, request.getStatus());
        return DriverProfileDto.fromEntity(updated);
    }

    @Override
    @Transactional
    public DriverProfileDto assignVehicle(UUID userId, UUID vehicleId) {
        DriverProfile profile = getDriverEntityByUserId(userId);
        profile.setCurrentVehicleId(vehicleId);
        DriverProfile updated = driverRepository.save(profile);
        log.info("Assigned vehicle {} to driver {}", vehicleId, profile.getId());
        return DriverProfileDto.fromEntity(updated);
    }
}
