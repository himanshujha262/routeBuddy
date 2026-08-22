package com.routbuddy.locations.service;

import com.routbuddy.common.domain.enums.TripStatus;
import com.routbuddy.locations.dto.TripOccupancyUpdate;
import com.routbuddy.trips.domain.entity.Trip;
import com.routbuddy.trips.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripOccupancyBroadcaster {

    private static final String HASH_KEY_OCCUPANCY_PREFIX = "routbuddy:trip:";

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final TripRepository tripRepository;

    public TripOccupancyUpdate broadcastOccupancy(UUID tripId) {
        Optional<Trip> tripOpt = tripRepository.findById(tripId);
        if (tripOpt.isEmpty()) {
            log.warn("Cannot broadcast occupancy: trip {} not found", tripId);
            return null;
        }

        Trip trip = tripOpt.get();
        return broadcastOccupancy(trip.getId(), trip.getTotalSeats(), trip.getAvailableSeats(), trip.getStatus());
    }

    public TripOccupancyUpdate broadcastOccupancy(UUID tripId, int totalSeats, int availableSeats, TripStatus status) {
        int bookedSeats = Math.max(0, totalSeats - availableSeats);
        double occupancyPercent = totalSeats > 0 ? (bookedSeats * 100.0) / totalSeats : 0.0;

        TripOccupancyUpdate update = TripOccupancyUpdate.builder()
                .tripId(tripId)
                .totalSeats(totalSeats)
                .availableSeats(availableSeats)
                .bookedSeats(bookedSeats)
                .occupancyPercentage(Math.round(occupancyPercent * 10.0) / 10.0)
                .status(status)
                .timestamp(Instant.now())
                .build();

        // 1. Cache in Redis
        try {
            String redisKey = HASH_KEY_OCCUPANCY_PREFIX + tripId + ":occupancy";
            Map<String, String> data = new HashMap<>();
            data.put("tripId", tripId.toString());
            data.put("totalSeats", String.valueOf(totalSeats));
            data.put("availableSeats", String.valueOf(availableSeats));
            data.put("bookedSeats", String.valueOf(bookedSeats));
            data.put("occupancyPercentage", String.valueOf(update.getOccupancyPercentage()));
            data.put("status", status != null ? status.name() : "UNKNOWN");
            data.put("timestamp", update.getTimestamp().toString());

            redisTemplate.opsForHash().putAll(redisKey, data);
        } catch (Exception e) {
            log.warn("Failed to cache trip occupancy in Redis: {}", e.getMessage());
        }

        // 2. Broadcast via WebSocket STOMP
        try {
            messagingTemplate.convertAndSend("/topic/trips/" + tripId + "/occupancy", update);
            messagingTemplate.convertAndSend("/topic/trip." + tripId + ".occupancy", update);
            log.info("Broadcasted occupancy for trip {}: {}/{} seats booked ({}%)",
                    tripId, bookedSeats, totalSeats, update.getOccupancyPercentage());
        } catch (Exception e) {
            log.error("Failed to broadcast occupancy update via WebSocket: {}", e.getMessage());
        }

        return update;
    }

    public TripOccupancyUpdate getLatestTripOccupancy(UUID tripId) {
        // 1. Try Redis cache
        try {
            String redisKey = HASH_KEY_OCCUPANCY_PREFIX + tripId + ":occupancy";
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(redisKey);
            if (!entries.isEmpty() && entries.containsKey("totalSeats")) {
                int totalSeats = Integer.parseInt((String) entries.get("totalSeats"));
                int availableSeats = Integer.parseInt((String) entries.get("availableSeats"));
                int bookedSeats = Integer.parseInt((String) entries.get("bookedSeats"));
                double occupancyPercent = Double.parseDouble((String) entries.get("occupancyPercentage"));
                String statusStr = (String) entries.get("status");
                TripStatus status = statusStr != null ? TripStatus.valueOf(statusStr) : TripStatus.PUBLISHED;
                String tsStr = (String) entries.get("timestamp");
                Instant ts = tsStr != null ? Instant.parse(tsStr) : Instant.now();

                return TripOccupancyUpdate.builder()
                        .tripId(tripId)
                        .totalSeats(totalSeats)
                        .availableSeats(availableSeats)
                        .bookedSeats(bookedSeats)
                        .occupancyPercentage(occupancyPercent)
                        .status(status)
                        .timestamp(ts)
                        .build();
            }
        } catch (Exception e) {
            log.warn("Failed to read occupancy from Redis: {}", e.getMessage());
        }

        // 2. Fallback to DB
        Optional<Trip> tripOpt = tripRepository.findById(tripId);
        if (tripOpt.isPresent()) {
            Trip trip = tripOpt.get();
            return broadcastOccupancy(trip.getId(), trip.getTotalSeats(), trip.getAvailableSeats(), trip.getStatus());
        }

        return null;
    }
}
