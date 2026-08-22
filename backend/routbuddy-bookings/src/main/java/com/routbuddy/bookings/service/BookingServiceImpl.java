package com.routbuddy.bookings.service;

import com.routbuddy.bookings.domain.entity.Booking;
import com.routbuddy.bookings.dto.*;
import com.routbuddy.bookings.repository.BookingRepository;
import com.routbuddy.common.domain.enums.BookingStatus;
import com.routbuddy.common.domain.enums.PaymentStatus;
import com.routbuddy.common.domain.enums.TripStatus;
import com.routbuddy.common.exception.BadRequestException;
import com.routbuddy.common.exception.ResourceNotFoundException;
import com.routbuddy.common.exception.UnauthorizedException;
import com.routbuddy.common.util.QRCodeCryptoUtil;
import com.routbuddy.drivers.domain.entity.DriverProfile;
import com.routbuddy.drivers.service.DriverService;
import com.routbuddy.routes.domain.entity.Route;
import com.routbuddy.routes.domain.entity.RouteStop;
import com.routbuddy.routes.repository.RouteRepository;
import com.routbuddy.routes.repository.RouteStopRepository;
import com.routbuddy.trips.domain.entity.Trip;
import com.routbuddy.trips.repository.TripRepository;
import com.routbuddy.vehicles.domain.entity.Vehicle;
import com.routbuddy.vehicles.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final BookingRepository bookingRepository;
    private final BookingStateMachine bookingStateMachine;
    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverService driverService;
    private final com.routbuddy.common.domain.event.EventPublisher eventPublisher;

    @Value("${jwt.secret:defaultSecretKeyWithAtLeast256BitsLengthForHmacSha256MustBeProvided}")
    private String qrSecretKey;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            BookingStateMachine bookingStateMachine,
            TripRepository tripRepository,
            RouteRepository routeRepository,
            RouteStopRepository routeStopRepository,
            VehicleRepository vehicleRepository,
            DriverService driverService,
            com.routbuddy.common.domain.event.EventPublisher eventPublisher) {
        this.bookingRepository = bookingRepository;
        this.bookingStateMachine = bookingStateMachine;
        this.tripRepository = tripRepository;
        this.routeRepository = routeRepository;
        this.routeStopRepository = routeStopRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverService = driverService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public BookingDto createBooking(UUID passengerId, CreateBookingRequest request) {
        // 1. Idempotency check
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<Booking> existing = bookingRepository.findByIdempotencyKey(request.getIdempotencyKey().trim());
            if (existing.isPresent()) {
                log.info("Idempotent booking request for key: {}. Returning existing booking: {}",
                        request.getIdempotencyKey(), existing.get().getId());
                return BookingDto.fromEntity(existing.get());
            }
        }

        // 2. Concurrency Protection: Acquire Pessimistic Write Lock on Trip row
        Trip trip = tripRepository.findByIdWithLock(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", request.getTripId()));

        if (trip.getStatus() != TripStatus.PUBLISHED && trip.getStatus() != TripStatus.BOARDING) {
            throw new BadRequestException("Trip is not open for bookings. Current status: " + trip.getStatus());
        }

        int requestedSeats = request.getSeatCount();
        if (requestedSeats <= 0) {
            throw new BadRequestException("Seat count must be at least 1");
        }

        // 3. Strict Capacity Validation
        if (trip.getAvailableSeats() < requestedSeats) {
            throw new BadRequestException(String.format(
                    "Insufficient seats available. Requested: %d, Available: %d",
                    requestedSeats, trip.getAvailableSeats()
            ));
        }

        // 4. Atomic seat decrement
        int updatedRows = tripRepository.decrementAvailableSeats(trip.getId(), requestedSeats);
        if (updatedRows == 0) {
            throw new BadRequestException("Failed to reserve seats due to concurrent booking. Please try again.");
        }
        trip.setAvailableSeats(trip.getAvailableSeats() - requestedSeats);

        // 5. Lookup Stops & Calculate Stage Fare
        RouteStop pickupStop = routeStopRepository.findById(request.getPickupStopId())
                .orElseThrow(() -> new ResourceNotFoundException("RouteStop", "id", request.getPickupStopId()));
        RouteStop dropoffStop = routeStopRepository.findById(request.getDropoffStopId())
                .orElseThrow(() -> new ResourceNotFoundException("RouteStop", "id", request.getDropoffStopId()));

        if (!pickupStop.getRoute().getId().equals(trip.getRouteId()) || !dropoffStop.getRoute().getId().equals(trip.getRouteId())) {
            throw new BadRequestException("Selected stops do not belong to the trip's route");
        }

        if (pickupStop.getSequenceOrder() >= dropoffStop.getSequenceOrder()) {
            throw new BadRequestException("Pickup stop must precede dropoff stop on the route");
        }

        double farePerSeat = Math.max(
                Math.abs(dropoffStop.getStageFareInr() - pickupStop.getStageFareInr()),
                trip.getFarePerSeatInr()
        );
        double totalFare = farePerSeat * requestedSeats;

        // 6. Generate Codes & Persist Booking
        String bookingCode = "RB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String otpCode = String.format("%06d", RANDOM.nextInt(1000000));

        Booking booking = Booking.builder()
                .bookingCode(bookingCode)
                .idempotencyKey(request.getIdempotencyKey() != null ? request.getIdempotencyKey().trim() : null)
                .tripId(trip.getId())
                .passengerId(passengerId)
                .pickupStopId(pickupStop.getId())
                .pickupStopName(pickupStop.getStopName())
                .dropoffStopId(dropoffStop.getId())
                .dropoffStopName(dropoffStop.getStopName())
                .seatCount(requestedSeats)
                .fareAmountInr(totalFare)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PAID)
                .bookingStatus(BookingStatus.CONFIRMED)
                .otpCode(otpCode)
                .build();

        Booking saved = bookingRepository.save(booking);

        long qrExpiry = Instant.now().plus(24, ChronoUnit.HOURS).toEpochMilli();
        String qrToken = QRCodeCryptoUtil.generateSignedBoardingPass(
                saved.getId(), trip.getId(), passengerId, qrExpiry, qrSecretKey
        );
        saved.setQrToken(qrToken);
        saved = bookingRepository.save(saved);

        log.info("Booking {} confirmed for passenger {} on trip {} (seats: {})",
                saved.getBookingCode(), passengerId, trip.getId(), requestedSeats);

        // Publish Occupancy Changed event for real-time WebSocket broadcast
        try {
            int newAvailable = trip.getAvailableSeats() - requestedSeats;
            eventPublisher.publish(new com.routbuddy.common.domain.event.TripOccupancyChangedEvent(
                    trip.getId(), trip.getTotalSeats(), newAvailable, trip.getStatus()
            ));
        } catch (Exception e) {
            log.warn("Failed to publish TripOccupancyChangedEvent for booking {}: {}", saved.getId(), e.getMessage());
        }

        return BookingDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public BookingDto cancelBooking(UUID passengerId, UUID bookingId, CancelBookingRequest request) {
        Booking booking = getBookingEntity(bookingId);

        if (!booking.getPassengerId().equals(passengerId)) {
            throw new UnauthorizedException("You are not authorized to cancel this booking");
        }

        // Validate state machine transition
        bookingStateMachine.transitionTo(booking, BookingStatus.CANCELLED);
        if (request != null && request.getReason() != null) {
            booking.setCancellationReason(request.getReason());
        }

        // Restore seats on Trip atomically
        tripRepository.incrementAvailableSeats(booking.getTripId(), booking.getSeatCount());

        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} cancelled by passenger {}. Restored {} seats to trip {}",
                booking.getBookingCode(), passengerId, booking.getSeatCount(), booking.getTripId());

        // Publish Occupancy Changed event for real-time WebSocket broadcast
        try {
            tripRepository.findById(booking.getTripId()).ifPresent(trip -> {
                eventPublisher.publish(new com.routbuddy.common.domain.event.TripOccupancyChangedEvent(
                        trip.getId(), trip.getTotalSeats(), trip.getAvailableSeats(), trip.getStatus()
                ));
            });
        } catch (Exception e) {
            log.warn("Failed to publish TripOccupancyChangedEvent after cancellation: {}", e.getMessage());
        }

        return BookingDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public BookingDto verifyBoarding(UUID driverUserId, VerifyBoardingRequest request) {
        DriverProfile driver = driverService.getDriverEntityByUserId(driverUserId);

        Booking booking;

        // Try QR token verification first
        if (request.getQrToken() != null && !request.getQrToken().isBlank()) {
            QRCodeCryptoUtil.BoardingPassPayload payload = QRCodeCryptoUtil.verifyAndExtractBoardingPass(
                    request.getQrToken().trim(), qrSecretKey
            );
            if (payload == null || !payload.isValid()) {
                throw new BadRequestException("Invalid or expired QR boarding pass");
            }
            booking = getBookingEntity(payload.bookingId());
        } else if (request.getOtpCode() != null && !request.getOtpCode().isBlank()) {
            if (request.getTripId() == null) {
                throw new BadRequestException("Trip ID is required when verifying with OTP");
            }
            booking = bookingRepository.findByTripIdAndOtpCode(request.getTripId(), request.getOtpCode().trim())
                    .orElseThrow(() -> new BadRequestException("Invalid OTP code for this trip"));
        } else {
            throw new BadRequestException("Either QR token or OTP code must be provided");
        }

        final Booking verifiedBooking = booking;
        final UUID tripId = verifiedBooking.getTripId();
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));

        if (!trip.getDriverId().equals(driver.getId())) {
            throw new UnauthorizedException("This booking belongs to another driver's trip");
        }

        bookingStateMachine.transitionTo(verifiedBooking, BookingStatus.BOARDED);
        Booking saved = bookingRepository.save(verifiedBooking);

        log.info("Passenger {} boarded trip {} for booking {}",
                saved.getPassengerId(), trip.getId(), saved.getBookingCode());

        return BookingDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getBookingById(UUID userId, UUID bookingId) {
        Booking booking = getBookingEntity(bookingId);
        if (!booking.getPassengerId().equals(userId)) {
            // Check if user is the driver
            Trip trip = tripRepository.findById(booking.getTripId()).orElse(null);
            if (trip != null) {
                try {
                    DriverProfile driver = driverService.getDriverEntityByUserId(userId);
                    if (!trip.getDriverId().equals(driver.getId())) {
                        throw new UnauthorizedException("You are not authorized to view this booking");
                    }
                } catch (Exception e) {
                    throw new UnauthorizedException("You are not authorized to view this booking");
                }
            }
        }
        return BookingDto.fromEntity(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDto> getMyBookings(UUID passengerId, Pageable pageable) {
        return bookingRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId, pageable)
                .map(BookingDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripSearchResultDto> searchTrips(TripSearchRequest request) {
        List<UUID> routeIds;
        if (request.getRouteId() != null) {
            routeIds = List.of(request.getRouteId());
        } else {
            routeIds = routeRepository.findByActiveTrue().stream().map(Route::getId).collect(Collectors.toList());
        }

        Instant fromTime = request.getDepartureAfter() != null ? request.getDepartureAfter() : Instant.now().minus(30, ChronoUnit.MINUTES);
        List<TripStatus> statuses = List.of(TripStatus.PUBLISHED, TripStatus.BOARDING);

        List<Trip> trips = tripRepository.findAvailableTripsForRoutes(routeIds, statuses, fromTime);

        int minSeats = request.getMinSeats() != null ? request.getMinSeats() : 1;

        return trips.stream()
                .filter(t -> t.getAvailableSeats() >= minSeats)
                .map(t -> {
                    Route route = routeRepository.findById(t.getRouteId()).orElse(null);
                    Vehicle vehicle = vehicleRepository.findById(t.getVehicleId()).orElse(null);

                    return TripSearchResultDto.builder()
                            .tripId(t.getId())
                            .routeId(t.getRouteId())
                            .routeName(route != null ? route.getName() : "Unknown Route")
                            .driverId(t.getDriverId())
                            .vehiclePlateNumber(vehicle != null ? vehicle.getPlateNumber() : "Unknown")
                            .vehicleType(vehicle != null ? vehicle.getVehicleType() : null)
                            .scheduledDeparture(t.getScheduledDeparture())
                            .status(t.getStatus())
                            .totalSeats(t.getTotalSeats())
                            .availableSeats(t.getAvailableSeats())
                            .farePerSeatInr(t.getFarePerSeatInr())
                            .liveLatitude(t.getLiveLatitude())
                            .liveLongitude(t.getLiveLongitude())
                            .liveSpeedKmph(t.getLiveSpeedKmph())
                            .currentStopSequence(t.getCurrentStopSequence())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getBookingEntity(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));
    }
}
