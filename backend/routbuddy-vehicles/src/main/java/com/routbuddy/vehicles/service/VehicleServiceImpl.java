package com.routbuddy.vehicles.service;

import com.routbuddy.common.exception.BadRequestException;
import com.routbuddy.common.exception.ResourceNotFoundException;
import com.routbuddy.common.exception.UserAlreadyExistsException;
import com.routbuddy.drivers.domain.entity.DriverProfile;
import com.routbuddy.drivers.service.DriverService;
import com.routbuddy.vehicles.domain.entity.Vehicle;
import com.routbuddy.vehicles.dto.RegisterVehicleRequest;
import com.routbuddy.vehicles.dto.VehicleDto;
import com.routbuddy.vehicles.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VehicleServiceImpl implements VehicleService {

    private static final Logger log = LoggerFactory.getLogger(VehicleServiceImpl.class);

    private final VehicleRepository vehicleRepository;
    private final DriverService driverService;

    public VehicleServiceImpl(VehicleRepository vehicleRepository, DriverService driverService) {
        this.vehicleRepository = vehicleRepository;
        this.driverService = driverService;
    }

    @Override
    @Transactional
    public VehicleDto registerVehicle(UUID userId, RegisterVehicleRequest request) {
        DriverProfile driver = driverService.getDriverEntityByUserId(userId);

        String plateNumber = request.getPlateNumber().trim().toUpperCase();
        if (vehicleRepository.existsByPlateNumber(plateNumber)) {
            throw new UserAlreadyExistsException("A vehicle with plate number " + plateNumber + " is already registered");
        }

        if (request.getSeatingCapacity() <= 0) {
            throw new BadRequestException("Seating capacity must be at least 1");
        }

        Vehicle vehicle = Vehicle.builder()
                .driverId(driver.getId())
                .plateNumber(plateNumber)
                .vehicleType(request.getVehicleType())
                .modelName(request.getModelName())
                .seatingCapacity(request.getSeatingCapacity())
                .fuelType(request.getFuelType())
                .rcNumber(request.getRcNumber())
                .permitNumber(request.getPermitNumber())
                .permitExpiryDate(request.getPermitExpiryDate())
                .insuranceExpiryDate(request.getInsuranceExpiryDate())
                .electric(request.isElectric())
                .approved(false)
                .vehiclePhotoUrl(request.getVehiclePhotoUrl())
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Registered vehicle with id: {}, plate: {} for driver: {}", saved.getId(), plateNumber, driver.getId());

        if (driver.getCurrentVehicleId() == null) {
            driverService.assignVehicle(userId, saved.getId());
        }

        return VehicleDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleDto> getVehiclesByDriver(UUID userId) {
        DriverProfile driver = driverService.getDriverEntityByUserId(userId);
        return vehicleRepository.findByDriverId(driver.getId()).stream()
                .map(VehicleDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleDto getVehicleById(UUID vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));
        return VehicleDto.fromEntity(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public Vehicle getVehicleEntity(UUID vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));
    }

    @Override
    @Transactional
    public VehicleDto approveVehicle(UUID vehicleId) {
        Vehicle vehicle = getVehicleEntity(vehicleId);
        vehicle.setApproved(true);
        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Admin approved vehicle id: {}, plate: {}", vehicleId, vehicle.getPlateNumber());
        return VehicleDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public VehicleDto rejectVehicle(UUID vehicleId) {
        Vehicle vehicle = getVehicleEntity(vehicleId);
        vehicle.setApproved(false);
        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Admin rejected vehicle id: {}, plate: {}", vehicleId, vehicle.getPlateNumber());
        return VehicleDto.fromEntity(saved);
    }
}
