package com.routbuddy.bookings.repository;

import com.routbuddy.bookings.domain.entity.Booking;
import com.routbuddy.common.domain.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    Optional<Booking> findByBookingCode(String bookingCode);
    Optional<Booking> findByIdempotencyKey(String idempotencyKey);
    Optional<Booking> findByQrToken(String qrToken);
    Optional<Booking> findByTripIdAndOtpCode(UUID tripId, String otpCode);
    List<Booking> findByTripIdAndBookingStatusIn(UUID tripId, List<BookingStatus> statuses);
    Page<Booking> findByPassengerIdOrderByCreatedAtDesc(UUID passengerId, Pageable pageable);
    long countByBookingStatus(BookingStatus status);
}
