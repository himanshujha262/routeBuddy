package com.routbuddy.trips;

import com.routbuddy.common.domain.enums.KYCStatus;
import com.routbuddy.common.domain.enums.TripStatus;
import com.routbuddy.common.domain.enums.VehicleType;
import com.routbuddy.common.exception.BadRequestException;
import com.routbuddy.drivers.domain.entity.DriverProfile;
import com.routbuddy.drivers.repository.DriverRepository;
import com.routbuddy.drivers.service.DriverService;
import com.routbuddy.routes.domain.entity.Route;
import com.routbuddy.routes.service.RouteService;
import com.routbuddy.trips.domain.entity.Trip;
import com.routbuddy.trips.dto.CreateTripRequest;
import com.routbuddy.trips.dto.TripDto;
import com.routbuddy.trips.repository.TripRepository;
import com.routbuddy.trips.service.TripService;
import com.routbuddy.trips.service.TripServiceImpl;
import com.routbuddy.trips.service.TripStateMachine;
import com.routbuddy.vehicles.domain.entity.Vehicle;
import com.routbuddy.vehicles.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private DriverService driverService;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private VehicleService vehicleService;

    @Mock
    private RouteService routeService;

    private TripStateMachine tripStateMachine;
    private TripService tripService;

    private UUID testUserId;
    private UUID testDriverId;
    private UUID testVehicleId;
    private UUID testRouteId;
    private UUID testTripId;

    private DriverProfile testDriver;
    private Vehicle testVehicle;
    private Route testRoute;
    private CreateTripRequest testRequest;
    private Trip testTrip;

    @BeforeEach
    void setUp() {
        tripStateMachine = new TripStateMachine();
        tripService = new TripServiceImpl(tripRepository, tripStateMachine, driverService, driverRepository, vehicleService, routeService);

        testUserId = UUID.randomUUID();
        testDriverId = UUID.randomUUID();
        testVehicleId = UUID.randomUUID();
        testRouteId = UUID.randomUUID();
        testTripId = UUID.randomUUID();

        testDriver = DriverProfile.builder()
                .id(testDriverId)
                .userId(testUserId)
                .kycStatus(KYCStatus.APPROVED)
                .online(true)
                .build();

        testVehicle = Vehicle.builder()
                .id(testVehicleId)
                .driverId(testDriverId)
                .vehicleType(VehicleType.AUTO_3W)
                .seatingCapacity(3)
                .approved(true)
                .build();

        testRoute = Route.builder()
                .id(testRouteId)
                .name("Sec 62 to Botanical Garden")
                .active(true)
                .build();

        testRequest = CreateTripRequest.builder()
                .vehicleId(testVehicleId)
                .routeId(testRouteId)
                .scheduledDeparture(Instant.now().plusSeconds(3600))
                .totalSeats(3)
                .farePerSeatInr(40.0)
                .build();

        testTrip = Trip.builder()
                .id(testTripId)
                .driverId(testDriverId)
                .vehicleId(testVehicleId)
                .routeId(testRouteId)
                .scheduledDeparture(Instant.now().plusSeconds(3600))
                .status(TripStatus.SCHEDULED)
                .totalSeats(3)
                .availableSeats(3)
                .farePerSeatInr(40.0)
                .build();
    }

    @Test
    @DisplayName("Should successfully create trip when driver KYC and vehicle are approved")
    void testCreateTrip_Success() {
        when(driverService.getDriverEntityByUserId(testUserId)).thenReturn(testDriver);
        when(vehicleService.getVehicleEntity(testVehicleId)).thenReturn(testVehicle);
        when(routeService.getRouteEntity(testRouteId)).thenReturn(testRoute);
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> {
            Trip t = invocation.getArgument(0);
            t.setId(testTripId);
            return t;
        });

        TripDto dto = tripService.createTrip(testUserId, testRequest);

        assertNotNull(dto);
        assertEquals(testTripId, dto.getId());
        assertEquals(TripStatus.SCHEDULED, dto.getStatus());
        assertEquals(3, dto.getTotalSeats());
        assertEquals(3, dto.getAvailableSeats());
        verify(tripRepository, times(1)).save(any(Trip.class));
    }

    @Test
    @DisplayName("Should reject trip creation if driver KYC is not approved")
    void testCreateTrip_DriverNotApproved_ThrowsException() {
        testDriver.setKycStatus(KYCStatus.PENDING);
        when(driverService.getDriverEntityByUserId(testUserId)).thenReturn(testDriver);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                tripService.createTrip(testUserId, testRequest)
        );

        assertTrue(ex.getMessage().contains("Driver KYC is not approved"));
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    @DisplayName("Should reject trip creation if vehicle is not approved")
    void testCreateTrip_VehicleNotApproved_ThrowsException() {
        testVehicle.setApproved(false);
        when(driverService.getDriverEntityByUserId(testUserId)).thenReturn(testDriver);
        when(vehicleService.getVehicleEntity(testVehicleId)).thenReturn(testVehicle);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                tripService.createTrip(testUserId, testRequest)
        );

        assertTrue(ex.getMessage().contains("Vehicle has not been approved"));
    }

    @Test
    @DisplayName("Should reject trip creation if requested capacity exceeds vehicle capacity")
    void testCreateTrip_CapacityExceeded_ThrowsException() {
        testRequest.setTotalSeats(5);
        when(driverService.getDriverEntityByUserId(testUserId)).thenReturn(testDriver);
        when(vehicleService.getVehicleEntity(testVehicleId)).thenReturn(testVehicle);
        when(routeService.getRouteEntity(testRouteId)).thenReturn(testRoute);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                tripService.createTrip(testUserId, testRequest)
        );

        assertTrue(ex.getMessage().contains("exceeds vehicle seating capacity"));
    }

    @Test
    @DisplayName("Should publish trip and transition to PUBLISHED")
    void testPublishTrip_Success() {
        when(tripRepository.findById(testTripId)).thenReturn(Optional.of(testTrip));
        when(driverService.getDriverEntityByUserId(testUserId)).thenReturn(testDriver);
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripDto dto = tripService.publishTrip(testUserId, testTripId);

        assertEquals(TripStatus.PUBLISHED, dto.getStatus());
    }
}
