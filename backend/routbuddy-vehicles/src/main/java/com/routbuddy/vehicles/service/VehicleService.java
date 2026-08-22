package com.routbuddy.vehicles.service;

import com.routbuddy.vehicles.domain.entity.Vehicle;
import com.routbuddy.vehicles.dto.RegisterVehicleRequest;
import com.routbuddy.vehicles.dto.VehicleDto;

import java.util.List;
import java.util.UUID;

public interface VehicleService {
    VehicleDto registerVehicle(UUID userId, RegisterVehicleRequest request);
    List<VehicleDto> getVehiclesByDriver(UUID userId);
    VehicleDto getVehicleById(UUID vehicleId);
    Vehicle getVehicleEntity(UUID vehicleId);
    VehicleDto approveVehicle(UUID vehicleId);
    VehicleDto rejectVehicle(UUID vehicleId);
}
