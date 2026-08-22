package com.routbuddy.vehicles;

import com.routbuddy.common.domain.enums.FuelType;
import com.routbuddy.common.domain.enums.VehicleType;
import com.routbuddy.common.exception.BadRequestException;
import com.routbuddy.common.exception.UserAlreadyExistsException;
import com.routbuddy.drivers.domain.entity.DriverProfile;
import com.routbuddy.drivers.service.DriverService;
import com.routbuddy.vehicles.domain.entity.Vehicle;
import com.routbuddy.vehicles.dto.RegisterVehicleRequest;
import com.routbuddy.vehicles.dto.VehicleDto;
import com.routbuddy.vehicles.repository.VehicleRepository;
import com.routbuddy.vehicles.service.VehicleService;
import com.routbuddy.vehicles.service.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DriverService driverService;

    private VehicleService vehicleService;

    private UUID testUserId;
    private UUID testDriverId;
    private UUID testVehicleId;
    private DriverProfile testDriver;
    private RegisterVehicleRequest testRequest;
    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleServiceImpl(vehicleRepository, driverService);
        testUserId = UUID.randomUUID();
        testDriverId = UUID.randomUUID();
        testVehicleId = UUID.randomUUID();

        testDriver = DriverProfile.builder()
                .id(testDriverId)
                .userId(testUserId)
                .build();

        testRequest = RegisterVehicleRequest.builder()
                .plateNumber("DL-01-AB-1234")
                .vehicleType(VehicleType.AUTO_3W)
                .modelName("Bajaj RE Compact")
                .seatingCapacity(3)
                .fuelType(FuelType.CNG)
                .build();

        testVehicle = Vehicle.builder()
                .id(testVehicleId)
                .driverId(testDriverId)
                .plateNumber("DL-01-AB-1234")
                .vehicleType(VehicleType.AUTO_3W)
                .modelName("Bajaj RE Compact")
                .seatingCapacity(3)
                .fuelType(FuelType.CNG)
                .approved(false)
                .build();
    }

    @Test
    @DisplayName("Should successfully register a vehicle for driver")
    void testRegisterVehicle_Success() {
        when(driverService.getDriverEntityByUserId(testUserId)).thenReturn(testDriver);
        when(vehicleRepository.existsByPlateNumber("DL-01-AB-1234")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle v = invocation.getArgument(0);
            v.setId(testVehicleId);
            return v;
        });

        VehicleDto dto = vehicleService.registerVehicle(testUserId, testRequest);

        assertNotNull(dto);
        assertEquals(testVehicleId, dto.getId());
        assertEquals("DL-01-AB-1234", dto.getPlateNumber());
        assertEquals(3, dto.getSeatingCapacity());
        assertFalse(dto.isApproved());
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should throw exception when vehicle plate number is duplicate")
    void testRegisterVehicle_DuplicatePlate_ThrowsException() {
        when(driverService.getDriverEntityByUserId(testUserId)).thenReturn(testDriver);
        when(vehicleRepository.existsByPlateNumber("DL-01-AB-1234")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> vehicleService.registerVehicle(testUserId, testRequest));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should throw exception when seating capacity is zero or negative")
    void testRegisterVehicle_InvalidCapacity_ThrowsException() {
        when(driverService.getDriverEntityByUserId(testUserId)).thenReturn(testDriver);
        when(vehicleRepository.existsByPlateNumber("DL-01-AB-1234")).thenReturn(false);
        testRequest.setSeatingCapacity(0);

        assertThrows(BadRequestException.class, () -> vehicleService.registerVehicle(testUserId, testRequest));
    }

    @Test
    @DisplayName("Should approve vehicle by Admin")
    void testApproveVehicle_Success() {
        when(vehicleRepository.findById(testVehicleId)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehicleDto dto = vehicleService.approveVehicle(testVehicleId);

        assertTrue(dto.isApproved());
        verify(vehicleRepository, times(1)).save(testVehicle);
    }
}
