package com.routbuddy.bookings.dto;

import java.time.Instant;
import java.util.UUID;

public class TripSearchRequest {

    private UUID routeId;
    private UUID pickupStopId;
    private UUID dropoffStopId;
    private Instant departureAfter;
    private Integer minSeats;

    public TripSearchRequest() {}

    public TripSearchRequest(UUID routeId, UUID pickupStopId, UUID dropoffStopId, Instant departureAfter, Integer minSeats) {
        this.routeId = routeId;
        this.pickupStopId = pickupStopId;
        this.dropoffStopId = dropoffStopId;
        this.departureAfter = departureAfter;
        this.minSeats = minSeats;
    }

    public UUID getRouteId() { return routeId; }
    public void setRouteId(UUID routeId) { this.routeId = routeId; }

    public UUID getPickupStopId() { return pickupStopId; }
    public void setPickupStopId(UUID pickupStopId) { this.pickupStopId = pickupStopId; }

    public UUID getDropoffStopId() { return dropoffStopId; }
    public void setDropoffStopId(UUID dropoffStopId) { this.dropoffStopId = dropoffStopId; }

    public Instant getDepartureAfter() { return departureAfter; }
    public void setDepartureAfter(Instant departureAfter) { this.departureAfter = departureAfter; }

    public Integer getMinSeats() { return minSeats; }
    public void setMinSeats(Integer minSeats) { this.minSeats = minSeats; }
}
