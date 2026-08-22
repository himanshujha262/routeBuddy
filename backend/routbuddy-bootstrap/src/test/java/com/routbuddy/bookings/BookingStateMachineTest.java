package com.routbuddy.bookings;

import com.routbuddy.bookings.domain.entity.Booking;
import com.routbuddy.bookings.service.BookingStateMachine;
import com.routbuddy.common.domain.enums.BookingStatus;
import com.routbuddy.common.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookingStateMachineTest {

    private BookingStateMachine stateMachine;
    private Booking booking;

    @BeforeEach
    void setUp() {
        stateMachine = new BookingStateMachine();
        booking = Booking.builder()
                .bookingCode("RB-TEST1234")
                .bookingStatus(BookingStatus.CONFIRMED)
                .build();
    }

    @Test
    @DisplayName("Should successfully transition CONFIRMED -> BOARDED -> COMPLETED")
    void testStandardHappyPathTransitions() {
        stateMachine.transitionTo(booking, BookingStatus.BOARDED);
        assertEquals(BookingStatus.BOARDED, booking.getBookingStatus());
        assertNotNull(booking.getBoardedAt());

        stateMachine.transitionTo(booking, BookingStatus.COMPLETED);
        assertEquals(BookingStatus.COMPLETED, booking.getBookingStatus());
        assertNotNull(booking.getCompletedAt());
    }

    @Test
    @DisplayName("Should allow cancellation from CONFIRMED state")
    void testCancellationFromConfirmed() {
        stateMachine.transitionTo(booking, BookingStatus.CANCELLED);
        assertEquals(BookingStatus.CANCELLED, booking.getBookingStatus());
        assertNotNull(booking.getCancelledAt());
    }

    @Test
    @DisplayName("Should reject illegal transition CONFIRMED -> COMPLETED without boarding")
    void testRejectDirectCompletionWithoutBoarding() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                stateMachine.transitionTo(booking, BookingStatus.COMPLETED)
        );
        assertTrue(ex.getMessage().contains("Illegal booking state transition"));
    }

    @Test
    @DisplayName("Should reject transition out of terminal state CANCELLED")
    void testRejectTransitionFromTerminalState() {
        stateMachine.transitionTo(booking, BookingStatus.CANCELLED);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                stateMachine.transitionTo(booking, BookingStatus.BOARDED)
        );
        assertTrue(ex.getMessage().contains("Illegal booking state transition"));
    }
}
