package com.routbuddy.bookings.controller;

import com.routbuddy.auth.security.UserPrincipal;
import com.routbuddy.bookings.dto.*;
import com.routbuddy.bookings.service.BookingService;
import com.routbuddy.common.dto.ApiResponse;
import com.routbuddy.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Seat reservation, QR ticketing, boarding verification, and booking lifecycle APIs")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/search")
    @Operation(summary = "Search available trips with real-time seat availability")
    public ResponseEntity<ApiResponse<List<TripSearchResultDto>>> searchTrips(
            @RequestParam(required = false) UUID routeId,
            @RequestParam(required = false) UUID pickupStopId,
            @RequestParam(required = false) UUID dropoffStopId,
            @RequestParam(required = false) Instant departureAfter,
            @RequestParam(required = false, defaultValue = "1") Integer minSeats) {
        TripSearchRequest request = new TripSearchRequest(routeId, pickupStopId, dropoffStopId, departureAfter, minSeats);
        List<TripSearchResultDto> results = bookingService.searchTrips(request);
        return ResponseEntity.ok(ApiResponse.ok("Trips retrieved successfully", results));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Reserve seats and create a confirmed booking with concurrency protection and idempotency")
    public ResponseEntity<ApiResponse<BookingDto>> createBooking(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateBookingRequest request) {
        BookingDto booking = bookingService.createBooking(userPrincipal.getId(), request);
        return new ResponseEntity<>(ApiResponse.ok("Booking confirmed successfully", booking), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get booking details and QR ticket by ID")
    public ResponseEntity<ApiResponse<BookingDto>> getBookingById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        BookingDto booking = bookingService.getBookingById(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Booking retrieved successfully", booking));
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get authenticated passenger's booking history with pagination")
    public ResponseEntity<ApiResponse<PageResponse<BookingDto>>> getMyBookings(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BookingDto> bookingPage = bookingService.getMyBookings(userPrincipal.getId(), PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok("Booking history retrieved successfully", PageResponse.from(bookingPage)));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel booking and release reserved seats")
    public ResponseEntity<ApiResponse<BookingDto>> cancelBooking(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id,
            @RequestBody(required = false) CancelBookingRequest request) {
        BookingDto booking = bookingService.cancelBooking(userPrincipal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.ok("Booking cancelled successfully", booking));
    }

    @PostMapping("/verify-boarding")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    @Operation(summary = "Driver scans QR code or validates OTP to board passenger")
    public ResponseEntity<ApiResponse<BookingDto>> verifyBoarding(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody VerifyBoardingRequest request) {
        BookingDto booking = bookingService.verifyBoarding(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Boarding verified successfully", booking));
    }
}
