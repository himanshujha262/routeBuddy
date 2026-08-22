package com.routbuddy.trips;

import com.routbuddy.common.domain.enums.TripStatus;
import com.routbuddy.common.exception.BadRequestException;
import com.routbuddy.trips.domain.entity.Trip;
import com.routbuddy.trips.service.TripStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TripStateMachineTest {

    private TripStateMachine stateMachine;
    private Trip trip;

    @BeforeEach
    void setUp() {
        stateMachine = new TripStateMachine();
        trip = Trip.builder()
                .id(UUID.randomUUID())
                .status(TripStatus.SCHEDULED)
                .totalSeats(4)
                .availableSeats(4)
                .build();
    }

    @Test
    @DisplayName("Should successfully follow the complete happy path lifecycle: SCHEDULED -> PUBLISHED -> BOARDING -> IN_TRANSIT -> COMPLETED")
    void testHappyPathTransitions() {
        assertEquals(TripStatus.SCHEDULED, trip.getStatus());

        // 1. Publish
        stateMachine.transitionTo(trip, TripStatus.PUBLISHED);
        assertEquals(TripStatus.PUBLISHED, trip.getStatus());

        // 2. Start Boarding
        stateMachine.transitionTo(trip, TripStatus.BOARDING);
        assertEquals(TripStatus.BOARDING, trip.getStatus());

        // 3. Depart (IN_TRANSIT)
        stateMachine.transitionTo(trip, TripStatus.IN_TRANSIT);
        assertEquals(TripStatus.IN_TRANSIT, trip.getStatus());
        assertNotNull(trip.getActualDeparture());

        // 4. Complete
        stateMachine.transitionTo(trip, TripStatus.COMPLETED);
        assertEquals(TripStatus.COMPLETED, trip.getStatus());
        assertNotNull(trip.getActualArrival());
    }

    @Test
    @DisplayName("Should allow cancellation from SCHEDULED, PUBLISHED, and BOARDING states")
    void testCancellationTransitions() {
        // From SCHEDULED
        Trip trip1 = Trip.builder().status(TripStatus.SCHEDULED).build();
        stateMachine.transitionTo(trip1, TripStatus.CANCELLED);
        assertEquals(TripStatus.CANCELLED, trip1.getStatus());

        // From PUBLISHED
        Trip trip2 = Trip.builder().status(TripStatus.PUBLISHED).build();
        stateMachine.transitionTo(trip2, TripStatus.CANCELLED);
        assertEquals(TripStatus.CANCELLED, trip2.getStatus());

        // From BOARDING
        Trip trip3 = Trip.builder().status(TripStatus.BOARDING).build();
        stateMachine.transitionTo(trip3, TripStatus.CANCELLED);
        assertEquals(TripStatus.CANCELLED, trip3.getStatus());
    }

    @Test
    @DisplayName("Should reject illegal state transitions")
    void testIllegalTransitions() {
        // Cannot jump directly from SCHEDULED to IN_TRANSIT
        assertThrows(BadRequestException.class, () ->
                stateMachine.transitionTo(trip, TripStatus.IN_TRANSIT)
        );

        // Cannot jump directly from SCHEDULED to COMPLETED
        assertThrows(BadRequestException.class, () ->
                stateMachine.transitionTo(trip, TripStatus.COMPLETED)
        );

        // Transition to COMPLETED
        stateMachine.transitionTo(trip, TripStatus.PUBLISHED);
        stateMachine.transitionTo(trip, TripStatus.BOARDING);
        stateMachine.transitionTo(trip, TripStatus.IN_TRANSIT);
        stateMachine.transitionTo(trip, TripStatus.COMPLETED);

        // Cannot transition out of COMPLETED
        assertThrows(BadRequestException.class, () ->
                stateMachine.transitionTo(trip, TripStatus.IN_TRANSIT)
        );
        assertThrows(BadRequestException.class, () ->
                stateMachine.transitionTo(trip, TripStatus.CANCELLED)
        );
    }

    @Test
    @DisplayName("Should validate seating capacity correctly against vehicle limit")
    void testCapacityValidation() {
        // Valid capacity
        assertDoesNotThrow(() -> stateMachine.validateCapacity(4, 4));
        assertDoesNotThrow(() -> stateMachine.validateCapacity(3, 4));

        // Capacity exceeding vehicle
        BadRequestException ex1 = assertThrows(BadRequestException.class, () ->
                stateMachine.validateCapacity(5, 4)
        );
        assertTrue(ex1.getMessage().contains("exceeds vehicle seating capacity"));

        // Non-positive capacity
        BadRequestException ex2 = assertThrows(BadRequestException.class, () ->
                stateMachine.validateCapacity(0, 4)
        );
        assertTrue(ex2.getMessage().contains("must be at least 1"));
    }
}
