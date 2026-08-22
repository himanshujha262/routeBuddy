package com.routbuddy.trips.service;

import com.routbuddy.common.domain.enums.TripStatus;
import com.routbuddy.common.exception.BadRequestException;
import com.routbuddy.trips.domain.entity.Trip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class TripStateMachine {

    private static final Logger log = LoggerFactory.getLogger(TripStateMachine.class);

    private static final Map<TripStatus, Set<TripStatus>> ALLOWED_TRANSITIONS = Map.of(
            TripStatus.SCHEDULED, EnumSet.of(TripStatus.PUBLISHED, TripStatus.CANCELLED),
            TripStatus.PUBLISHED, EnumSet.of(TripStatus.BOARDING, TripStatus.CANCELLED),
            TripStatus.BOARDING, EnumSet.of(TripStatus.IN_TRANSIT, TripStatus.CANCELLED),
            TripStatus.IN_TRANSIT, EnumSet.of(TripStatus.COMPLETED),
            TripStatus.COMPLETED, EnumSet.noneOf(TripStatus.class),
            TripStatus.CANCELLED, EnumSet.noneOf(TripStatus.class)
    );

    public void validateTransition(Trip trip, TripStatus targetStatus) {
        TripStatus currentStatus = trip.getStatus();
        Set<TripStatus> validTargets = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, EnumSet.noneOf(TripStatus.class));

        if (!validTargets.contains(targetStatus)) {
            String errorMsg = String.format(
                    "Illegal trip state transition from [%s] to [%s]. Allowed transitions from [%s]: %s",
                    currentStatus, targetStatus, currentStatus, validTargets
            );
            log.warn(errorMsg);
            throw new BadRequestException(errorMsg);
        }
    }

    public void transitionTo(Trip trip, TripStatus targetStatus) {
        validateTransition(trip, targetStatus);
        TripStatus previousStatus = trip.getStatus();
        trip.setStatus(targetStatus);

        Instant now = Instant.now();
        if (targetStatus == TripStatus.IN_TRANSIT && trip.getActualDeparture() == null) {
            trip.setActualDeparture(now);
        } else if (targetStatus == TripStatus.COMPLETED && trip.getActualArrival() == null) {
            trip.setActualArrival(now);
        }

        log.info("Trip [{}] transitioned from [{}] to [{}]", trip.getId(), previousStatus, targetStatus);
    }

    public void validateCapacity(int requestedSeats, int vehicleCapacity) {
        if (requestedSeats <= 0) {
            throw new BadRequestException("Trip seating capacity must be at least 1");
        }
        if (requestedSeats > vehicleCapacity) {
            throw new BadRequestException(String.format(
                    "Requested trip capacity [%d] exceeds vehicle seating capacity [%d]",
                    requestedSeats, vehicleCapacity
            ));
        }
    }
}
