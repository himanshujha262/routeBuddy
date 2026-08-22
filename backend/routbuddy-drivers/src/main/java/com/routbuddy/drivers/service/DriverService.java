package com.routbuddy.drivers.service;

import com.routbuddy.drivers.domain.entity.DriverProfile;
import com.routbuddy.drivers.dto.DriverProfileDto;
import com.routbuddy.drivers.dto.RegisterDriverRequest;
import com.routbuddy.drivers.dto.UpdateKycRequest;

import java.util.UUID;

public interface DriverService {
    DriverProfileDto registerDriver(UUID userId, RegisterDriverRequest request);
    DriverProfileDto getDriverProfileByUserId(UUID userId);
    DriverProfileDto getDriverProfile(UUID driverId);
    DriverProfile getDriverEntity(UUID driverId);
    DriverProfile getDriverEntityByUserId(UUID userId);
    DriverProfileDto updateOnlineStatus(UUID userId, boolean online);
    DriverProfileDto updateKycStatus(UUID driverId, UpdateKycRequest request);
    DriverProfileDto assignVehicle(UUID userId, UUID vehicleId);
}
