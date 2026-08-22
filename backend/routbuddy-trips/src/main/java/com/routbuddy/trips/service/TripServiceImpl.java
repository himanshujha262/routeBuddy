package com.routbuddy.trips.service;

import com.routbuddy.common.domain.enums.KYCStatus;
import com.routbuddy.common.domain.enums.TripStatus;
import com.routbuddy.common.exception.BadRequestException;
import com.routbuddy.common.exception.ResourceNotFoundException;
import com.routbuddy.common.exception.UnauthorizedException;
import com.routbuddy.drivers.domain.entity.DriverProfile;
import com.routbuddy.drivers.repository.DriverRepository;
import com.routbuddy.drivers.service.DriverService;
import com.routbuddy.routes.domain.entity.Route;
import com.routbuddy.routes.service.RouteService;
import com.routbuddy.trips.domain.entity.Trip;
import com.routbuddy.trips.dto.CreateTripRequest;
import com.routbuddy.trips.dto.TripDto;
import com.routbuddy.trips.dto.UpdateTelemetryRequest;
import com.routbuddy.trips.repository.TripRepository;
import com.routbuddy.vehicles.domain.entity.Vehicle;
import com.routbuddy.vehicles.service.VehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TripServiceImpl implements TripService {

    private static final Logger log = LoggerFactory.getLogger(TripServiceImpl.class);

    private final TripRepository tripRepository;
    private final TripStateMachine tripStateMachine;
    private final DriverService driverService;
    private final DriverRepository driverRepository;
    private final VehicleService vehicleService;
    private final RouteService routeService;

    public TripServiceImpl(
            TripRepository tripRepository,
            TripStateMachine tripStateMachine,
            DriverService driverService,
            DriverRepository driverRepository,
            VehicleService vehicleService,
            RouteService routeService) {
        this.tripRepository = tripRepository;
        this.tripStateMachine = tripStateMachine;
        this.driverService = driverService;
        this.driverRepository = driverRepository;
        this.vehicleService = vehicleService;
        this.routeService = routeService;
    }

    @Override
    @Transactional
    public TripDto createTrip(UUID userId, CreateTripRequest request) {
        DriverProfile driver = driverService.getDriverEntityByUserId(userId);

        if (driver.getKycStatus() != KYCStatus.APPROVED) {
            throw new BadRequestException("Driver KYC is not approved. Current status: " + driver.getKycStatus());
        }

        Vehicle vehicle = vehicleService.getVehicleEntity(request.getVehicleId());
        if (!vehicle.getDriverId().equals(driver.getId())) {
            throw new UnauthorizedException("Vehicle does not belong to this driver");
        }
        if (!vehicle.isApproved()) {
            throw new BadRequestException("Vehicle has not been approved by platform admins");
        }

        Route route = routeService.getRouteEntity(request.getRouteId());
        if (!route.isActive()) {
            throw new BadRequestException("Cannot create trip for an inactive route");
        }

        // Validate capacity
        tripStateMachine.validateCapacity(request.getTotalSeats(), vehicle.getSeatingCapacity());

        Trip trip = Trip.builder()
                .driverId(driver.getId())
                .vehicleId(vehicle.getId())
                .routeId(route.getId())
                .scheduledDeparture(request.getScheduledDeparture())
                .status(TripStatus.SCHEDULED)
                .totalSeats(request.getTotalSeats())
                .availableSeats(request.getTotalSeats())
                .farePerSeatInr(request.getFarePerSeatInr())
                .currentStopSequence(0)
                .build();

        Trip saved = tripRepository.save(trip);
        log.info("Created trip {} for driver {} on route {}", saved.getId(), driver.getId(), route.getId());
        return TripDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public TripDto publishTrip(UUID userId, UUID tripId) {
        Trip trip = getTripAndVerifyDriver(userId, tripId);
        tripStateMachine.transitionTo(trip, TripStatus.PUBLISHED);
        Trip saved = tripRepository.save(trip);
        return TripDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public TripDto startBoarding(UUID userId, UUID tripId) {
        Trip trip = getTripAndVerifyDriver(userId, tripId);
        tripStateMachine.transitionTo(trip, TripStatus.BOARDING);
        Trip saved = tripRepository.save(trip);
        return TripDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public TripDto startTrip(UUID userId, UUID tripId) {
        Trip trip = getTripAndVerifyDriver(userId, tripId);
        tripStateMachine.transitionTo(trip, TripStatus.IN_TRANSIT);
        Trip saved = tripRepository.save(trip);
        return TripDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public TripDto completeTrip(UUID userId, UUID tripId) {
        Trip trip = getTripAndVerifyDriver(userId, tripId);
        tripStateMachine.transitionTo(trip, TripStatus.COMPLETED);
        Trip saved = tripRepository.save(trip);

        // Update driver stats
        DriverProfile driver = driverService.getDriverEntity(trip.getDriverId());
        driver.setTotalTripsCompleted(driver.getTotalTripsCompleted() + 1);
        driverRepository.save(driver);

        log.info("Trip {} completed by driver {}", tripId, driver.getId());
        return TripDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public TripDto cancelTrip(UUID userId, UUID tripId) {
        Trip trip = getTripAndVerifyDriver(userId, tripId);
        tripStateMachine.transitionTo(trip, TripStatus.CANCELLED);
        Trip saved = tripRepository.save(trip);
        return TripDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public TripDto updateTelemetry(UUID userId, UUID tripId, UpdateTelemetryRequest request) {
        Trip trip = getTripAndVerifyDriver(userId, tripId);

        if (trip.getStatus() != TripStatus.IN_TRANSIT && trip.getStatus() != TripStatus.BOARDING) {
            throw new BadRequestException("Telemetry can only be updated for trips in BOARDING or IN_TRANSIT state");
        }

        trip.setLiveLatitude(request.getLatitude());
        trip.setLiveLongitude(request.getLongitude());
        if (request.getHeading() != null) trip.setLiveHeading(request.getHeading());
        if (request.getSpeedKmph() != null) trip.setLiveSpeedKmph(request.getSpeedKmph());
        if (request.getCurrentStopSequence() != null) trip.setCurrentStopSequence(request.getCurrentStopSequence());
        trip.setLastTelemetryPingAt(Instant.now());

        Trip saved = tripRepository.save(trip);
        return TripDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TripDto getTripById(UUID tripId) {
        Trip trip = getTripEntity(tripId);
        return TripDto.fromEntity(trip);
    }

    @Override
    @Transactional(readOnly = true)
    public Trip getTripEntity(UUID tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripDto> getAvailableTripsForRoute(UUID routeId) {
        List<TripStatus> bookableStatuses = List.of(TripStatus.PUBLISHED, TripStatus.BOARDING);
        return tripRepository.findByRouteIdAndStatusIn(routeId, bookableStatuses).stream()
                .filter(t -> t.getAvailableSeats() > 0)
                .map(TripDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TripDto getActiveTripForDriver(UUID userId) {
        DriverProfile driver = driverService.getDriverEntityByUserId(userId);
        List<TripStatus> activeStatuses = List.of(TripStatus.SCHEDULED, TripStatus.PUBLISHED, TripStatus.BOARDING, TripStatus.IN_TRANSIT);
        Trip trip = tripRepository.findFirstByDriverIdAndStatusInOrderByScheduledDepartureDesc(driver.getId(), activeStatuses)
                .orElseThrow(() -> new ResourceNotFoundException("No active trip found for driver: " + driver.getId()));
        return TripDto.fromEntity(trip);
    }

    private Trip getTripAndVerifyDriver(UUID userId, UUID tripId) {
        Trip trip = getTripEntity(tripId);
        DriverProfile driver = driverService.getDriverEntityByUserId(userId);
        if (!trip.getDriverId().equals(driver.getId())) {
            throw new UnauthorizedException("You are not authorized to modify this trip");
        }
        return trip;
    }
}
