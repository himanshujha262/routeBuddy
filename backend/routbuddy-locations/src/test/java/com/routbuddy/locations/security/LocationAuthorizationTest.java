package com.routbuddy.locations.security;

import com.routbuddy.auth.security.UserPrincipal;
import com.routbuddy.common.domain.enums.TripStatus;
import com.routbuddy.common.domain.enums.UserRole;
import com.routbuddy.common.exception.ResourceNotFoundException;
import com.routbuddy.drivers.domain.entity.DriverProfile;
import com.routbuddy.drivers.repository.DriverProfileRepository;
import com.routbuddy.locations.controller.LocationWebSocketController;
import com.routbuddy.locations.dto.DriverLocationPing;
import com.routbuddy.locations.service.LocationTrackingService;
import com.routbuddy.trips.domain.entity.Trip;
import com.routbuddy.trips.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationAuthorizationTest {

    @Mock
    private LocationTrackingService locationTrackingService;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private DriverProfileRepository driverProfileRepository;

    private LocationWebSocketController controller;

    private UUID driverProfileId;
    private UUID driverUserId;
    private UUID tripId;
    private Trip activeTrip;
    private UsernamePasswordAuthenticationToken authPrincipal;

    @BeforeEach
    void setUp() {
        controller = new LocationWebSocketController(locationTrackingService, tripRepository, driverProfileRepository);

        driverProfileId = UUID.randomUUID();
        driverUserId = UUID.randomUUID();
        tripId = UUID.randomUUID();

        activeTrip = Trip.builder()
                .id(tripId)
                .driverId(driverProfileId)
                .vehicleId(UUID.randomUUID())
                .routeId(UUID.randomUUID())
                .scheduledDeparture(Instant.now())
                .status(TripStatus.IN_TRANSIT)
                .totalSeats(3)
                .availableSeats(1)
                .farePerSeatInr(20.0)
                .build();

        UserPrincipal principal = new UserPrincipal(
                driverUserId,
                "9876543210",
                "driver@routbuddy.com",
                "secret",
                "Raju Driver",
                UserRole.DRIVER,
                Set.of("ROLE_DRIVER"),
                true
        );

        authPrincipal = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Test
    @DisplayName("Authorized driver can publish location for active trip")
    void testAuthorizedDriverCanPublishLocation() {
        DriverProfile driverProfile = DriverProfile.builder()
                .id(driverProfileId)
                .userId(driverUserId)
                .licenseNumber("DL-123456789")
                .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(activeTrip));
        when(driverProfileRepository.findByUserId(driverUserId)).thenReturn(Optional.of(driverProfile));

        DriverLocationPing ping = DriverLocationPing.builder()
                .latitude(28.6139)
                .longitude(77.2090)
                .speedKmph(30.0)
                .build();

        assertDoesNotThrow(() -> controller.handleTripLocationPing(tripId, ping, authPrincipal));

        verify(locationTrackingService).processLocationPing(any(DriverLocationPing.class));
    }

    @Test
    @DisplayName("Non-assigned driver is rejected with AccessDeniedException")
    void testNonAssignedDriverIsRejected() {
        UUID otherDriverProfileId = UUID.randomUUID();
        DriverProfile otherDriver = DriverProfile.builder()
                .id(otherDriverProfileId)
                .userId(driverUserId)
                .licenseNumber("DL-999999999")
                .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(activeTrip));
        when(driverProfileRepository.findByUserId(driverUserId)).thenReturn(Optional.of(otherDriver));

        DriverLocationPing ping = DriverLocationPing.builder()
                .latitude(28.6139)
                .longitude(77.2090)
                .build();

        assertThrows(AccessDeniedException.class, () ->
                controller.handleTripLocationPing(tripId, ping, authPrincipal)
        );

        verify(locationTrackingService, never()).processLocationPing(any());
    }

    @Test
    @DisplayName("Location ping for inactive trip is rejected")
    void testInactiveTripIsRejected() {
        Trip completedTrip = Trip.builder()
                .id(tripId)
                .driverId(driverProfileId)
                .vehicleId(UUID.randomUUID())
                .routeId(UUID.randomUUID())
                .scheduledDeparture(Instant.now())
                .status(TripStatus.COMPLETED)
                .totalSeats(3)
                .availableSeats(0)
                .farePerSeatInr(20.0)
                .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(completedTrip));

        DriverLocationPing ping = DriverLocationPing.builder()
                .latitude(28.6139)
                .longitude(77.2090)
                .build();

        assertThrows(AccessDeniedException.class, () ->
                controller.handleTripLocationPing(tripId, ping, authPrincipal)
        );

        verify(locationTrackingService, never()).processLocationPing(any());
    }

    @Test
    @DisplayName("Non-existent trip throws ResourceNotFoundException")
    void testNonExistentTripThrowsNotFound() {
        when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

        DriverLocationPing ping = DriverLocationPing.builder()
                .latitude(28.6139)
                .longitude(77.2090)
                .build();

        assertThrows(ResourceNotFoundException.class, () ->
                controller.handleTripLocationPing(tripId, ping, authPrincipal)
        );
    }
}
