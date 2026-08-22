package com.routbuddy.common.domain.event;

import com.routbuddy.common.domain.enums.TripStatus;
import lombok.Getter;

import java.util.UUID;

@Getter
public class TripOccupancyChangedEvent extends DomainEvent {

    private final UUID tripId;
    private final int totalSeats;
    private final int availableSeats;
    private final TripStatus status;

    public TripOccupancyChangedEvent(UUID tripId, int totalSeats, int availableSeats, TripStatus status) {
        super("TRIP_OCCUPANCY_CHANGED");
        this.tripId = tripId;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.status = status;
    }
}
