package com.routbuddy.locations.dto;

import jakarta.validation.constraints.NotNull;
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
public class DriverLocationPing {

    @NotNull(message = "Driver ID is required")
    private UUID driverId;

    @NotNull(message = "Trip ID is required")
    private UUID tripId;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private Double heading;
    private Double speedKmph;
    private Double accuracyMeters;
    private Integer currentStopSequence;

    @Builder.Default
    private Instant timestamp = Instant.now();
}
