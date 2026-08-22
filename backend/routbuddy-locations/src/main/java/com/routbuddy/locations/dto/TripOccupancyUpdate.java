package com.routbuddy.locations.dto;

import com.routbuddy.common.domain.enums.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripOccupancyUpdate {

    private UUID tripId;
    private int totalSeats;
    private int availableSeats;
    private int bookedSeats;
    private double occupancyPercentage;
    private TripStatus status;

    @Builder.Default
    private Instant timestamp = Instant.now();
}
