package com.routbuddy.bookings.service;

import com.routbuddy.bookings.domain.entity.Booking;
import com.routbuddy.bookings.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface BookingService {
    BookingDto createBooking(UUID passengerId, CreateBookingRequest request);
    BookingDto cancelBooking(UUID passengerId, UUID bookingId, CancelBookingRequest request);
    BookingDto verifyBoarding(UUID driverUserId, VerifyBoardingRequest request);
    BookingDto getBookingById(UUID userId, UUID bookingId);
    Page<BookingDto> getMyBookings(UUID passengerId, Pageable pageable);
    List<TripSearchResultDto> searchTrips(TripSearchRequest request);
    Booking getBookingEntity(UUID bookingId);
}
