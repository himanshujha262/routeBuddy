package com.routbuddy.bookings;

import com.routbuddy.bookings.domain.entity.Booking;
import com.routbuddy.bookings.dto.BookingDto;
import com.routbuddy.bookings.dto.CancelBookingRequest;
import com.routbuddy.bookings.dto.CreateBookingRequest;
import com.routbuddy.bookings.repository.BookingRepository;
import com.routbuddy.bookings.service.BookingService;
import com.routbuddy.bookings.service.BookingServiceImpl;
import com.routbuddy.bookings.service.BookingStateMachine;
import com.routbuddy.common.domain.enums.BookingStatus;
import com.routbuddy.common.domain.enums.PaymentMethod;
import com.routbuddy.common.domain.enums.TripStatus;
import com.routbuddy.common.exception.BadRequestException;
import com.routbuddy.drivers.service.DriverService;
import com.routbuddy.routes.domain.entity.Route;
import com.routbuddy.routes.domain.entity.RouteStop;
import com.routbuddy.routes.repository.RouteRepository;
import com.routbuddy.routes.repository.RouteStopRepository;
import com.routbuddy.trips.domain.entity.Trip;
import com.routbuddy.trips.repository.TripRepository;
import com.routbuddy.vehicles.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private RouteStopRepository routeStopRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DriverService driverService;

    private BookingStateMachine bookingStateMachine;
    private BookingService bookingService;

    private UUID passengerId;
    private UUID tripId;
    private UUID routeId;
    private UUID pickupStopId;
    private UUID dropoffStopId;
    private UUID bookingId;

    private Trip testTrip;
    private Route testRoute;
    private RouteStop pickupStop;
    private RouteStop dropoffStop;
    private CreateBookingRequest createRequest;

    @BeforeEach
    void setUp() {
        bookingStateMachine = new BookingStateMachine();
        bookingService = new BookingServiceImpl(
                bookingRepository,
                bookingStateMachine,
                tripRepository,
                routeRepository,
                routeStopRepository,
                vehicleRepository,
                driverService
        );
        ReflectionTestUtils.setField(bookingService, "qrSecretKey", "testSecretKeyForHmacSha256MustBeAtLeast256BitsLong!!");

        passengerId = UUID.randomUUID();
        tripId = UUID.randomUUID();
        routeId = UUID.randomUUID();
        pickupStopId = UUID.randomUUID();
        dropoffStopId = UUID.randomUUID();
        bookingId = UUID.randomUUID();

        testRoute = Route.builder()
                .id(routeId)
                .name("Sec 62 to Botanical Garden")
                .active(true)
                .build();

        pickupStop = RouteStop.builder()
                .id(pickupStopId)
                .route(testRoute)
                .stopName("Noida Sec 62")
                .sequenceOrder(1)
                .stageFareInr(10.0)
                .build();

        dropoffStop = RouteStop.builder()
                .id(dropoffStopId)
                .route(testRoute)
                .stopName("Botanical Garden")
                .sequenceOrder(4)
                .stageFareInr(40.0)
                .build();

        testTrip = Trip.builder()
                .id(tripId)
                .routeId(routeId)
                .driverId(UUID.randomUUID())
                .vehicleId(UUID.randomUUID())
                .status(TripStatus.PUBLISHED)
                .totalSeats(4)
                .availableSeats(4)
                .farePerSeatInr(30.0)
                .scheduledDeparture(Instant.now().plusSeconds(3600))
                .build();

        createRequest = CreateBookingRequest.builder()
                .tripId(tripId)
                .pickupStopId(pickupStopId)
                .dropoffStopId(dropoffStopId)
                .seatCount(2)
                .paymentMethod(PaymentMethod.UPI_INTENT)
                .idempotencyKey("idemp-key-12345")
                .build();
    }

    @Test
    @DisplayName("Should successfully create a booking with pessimistic lock and atomic seat decrement")
    void testCreateBooking_Success() {
        when(bookingRepository.findByIdempotencyKey("idemp-key-12345")).thenReturn(Optional.empty());
        when(tripRepository.findByIdWithLock(tripId)).thenReturn(Optional.of(testTrip));
        when(tripRepository.decrementAvailableSeats(tripId, 2)).thenReturn(1);
        when(routeStopRepository.findById(pickupStopId)).thenReturn(Optional.of(pickupStop));
        when(routeStopRepository.findById(dropoffStopId)).thenReturn(Optional.of(dropoffStop));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(bookingId);
            return b;
        });

        BookingDto dto = bookingService.createBooking(passengerId, createRequest);

        assertNotNull(dto);
        assertEquals(bookingId, dto.getId());
        assertEquals(BookingStatus.CONFIRMED, dto.getBookingStatus());
        assertEquals(2, dto.getSeatCount());
        assertEquals(60.0, dto.getFareAmountInr()); // 30.0 * 2
        assertNotNull(dto.getQrToken());
        assertNotNull(dto.getOtpCode());
        verify(tripRepository, times(1)).decrementAvailableSeats(tripId, 2);
    }

    @Test
    @DisplayName("Should return existing booking when duplicate idempotency key is received")
    void testCreateBooking_Idempotency_ReturnsExisting() {
        Booking existingBooking = Booking.builder()
                .id(bookingId)
                .bookingCode("RB-EXISTING")
                .idempotencyKey("idemp-key-12345")
                .tripId(tripId)
                .passengerId(passengerId)
                .seatCount(2)
                .bookingStatus(BookingStatus.CONFIRMED)
                .build();

        when(bookingRepository.findByIdempotencyKey("idemp-key-12345")).thenReturn(Optional.of(existingBooking));

        BookingDto dto = bookingService.createBooking(passengerId, createRequest);

        assertNotNull(dto);
        assertEquals(bookingId, dto.getId());
        assertEquals("RB-EXISTING", dto.getBookingCode());
        verify(tripRepository, never()).findByIdWithLock(any());
        verify(tripRepository, never()).decrementAvailableSeats(any(), anyInt());
    }

    @Test
    @DisplayName("Should reject booking when requested seats exceed available seats")
    void testCreateBooking_InsufficientSeats_ThrowsException() {
        testTrip.setAvailableSeats(1);
        when(bookingRepository.findByIdempotencyKey("idemp-key-12345")).thenReturn(Optional.empty());
        when(tripRepository.findByIdWithLock(tripId)).thenReturn(Optional.of(testTrip));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                bookingService.createBooking(passengerId, createRequest)
        );

        assertTrue(ex.getMessage().contains("Insufficient seats available"));
        verify(tripRepository, never()).decrementAvailableSeats(any(), anyInt());
    }

    @Test
    @DisplayName("Should cancel booking and restore available seats to trip")
    void testCancelBooking_RestoresSeats() {
        Booking booking = Booking.builder()
                .id(bookingId)
                .bookingCode("RB-CANCEL12")
                .tripId(tripId)
                .passengerId(passengerId)
                .seatCount(2)
                .bookingStatus(BookingStatus.CONFIRMED)
                .build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingDto dto = bookingService.cancelBooking(passengerId, bookingId, new CancelBookingRequest("Plans changed"));

        assertEquals(BookingStatus.CANCELLED, dto.getBookingStatus());
        assertEquals("Plans changed", dto.getCancellationReason());
        verify(tripRepository, times(1)).incrementAvailableSeats(tripId, 2);
    }
}
