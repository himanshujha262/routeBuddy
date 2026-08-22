package com.routbuddy.locations.service;

import com.routbuddy.common.domain.event.EventPublisher;
import com.routbuddy.locations.dto.DriverLocationPing;
import com.routbuddy.trips.domain.entity.Trip;
import com.routbuddy.trips.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
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
public class LocationTrackingService {

    private static final String GEO_KEY_ACTIVE_DRIVERS = "routbuddy:geo:active_drivers";
    private static final String HASH_KEY_TRIP_PREFIX = "routbuddy:trip:";

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final TripRepository tripRepository;
    private final EventPublisher eventPublisher;

    public void processLocationPing(DriverLocationPing ping) {
        log.debug("Processing telemetry ping for trip={}, driver={}, lat={}, lon={}",
                ping.getTripId(), ping.getDriverId(), ping.getLatitude(), ping.getLongitude());

        // 1. Update Redis Geospatial Index for nearby driver discovery
        try {
            redisTemplate.opsForGeo().add(
                    GEO_KEY_ACTIVE_DRIVERS,
                    new Point(ping.getLongitude(), ping.getLatitude()),
                    ping.getDriverId().toString()
            );

            // 2. Store latest telemetry in Redis Hash for fast sub-millisecond retrieval
            String tripKey = HASH_KEY_TRIP_PREFIX + ping.getTripId() + ":telemetry";
            Map<String, String> telemetryData = new HashMap<>();
            telemetryData.put("driverId", ping.getDriverId().toString());
            telemetryData.put("tripId", ping.getTripId().toString());
            telemetryData.put("latitude", String.valueOf(ping.getLatitude()));
            telemetryData.put("longitude", String.valueOf(ping.getLongitude()));
            telemetryData.put("heading", String.valueOf(ping.getHeading() != null ? ping.getHeading() : 0.0));
            telemetryData.put("speedKmph", String.valueOf(ping.getSpeedKmph() != null ? ping.getSpeedKmph() : 0.0));
            if (ping.getAccuracyMeters() != null) {
                telemetryData.put("accuracyMeters", String.valueOf(ping.getAccuracyMeters()));
            }
            if (ping.getCurrentStopSequence() != null) {
                telemetryData.put("currentStopSequence", String.valueOf(ping.getCurrentStopSequence()));
            }
            telemetryData.put("timestamp", ping.getTimestamp() != null ? ping.getTimestamp().toString() : Instant.now().toString());

            redisTemplate.opsForHash().putAll(tripKey, telemetryData);
        } catch (Exception e) {
            log.warn("Redis telemetry caching warning: {}", e.getMessage());
        }

        // 3. Update Trip entity coordinates asynchronously/safely
        try {
            tripRepository.findById(ping.getTripId()).ifPresent(trip -> {
                trip.setLiveLatitude(ping.getLatitude());
                trip.setLiveLongitude(ping.getLongitude());
                trip.setLiveHeading(ping.getHeading());
                trip.setLiveSpeedKmph(ping.getSpeedKmph());
                trip.setLastTelemetryPingAt(ping.getTimestamp() != null ? ping.getTimestamp() : Instant.now());
                if (ping.getCurrentStopSequence() != null) {
                    trip.setCurrentStopSequence(ping.getCurrentStopSequence());
                }
                tripRepository.save(trip);
            });
        } catch (Exception e) {
            log.warn("Failed to persist trip telemetry to database: {}", e.getMessage());
        }

        // 4. Broadcast real-time location to WebSocket subscribers
        // Topic 1: Standard REST-like topic pattern
        messagingTemplate.convertAndSend("/topic/trips/" + ping.getTripId() + "/location", ping);

        // Topic 2: Dot notation topic pattern
        messagingTemplate.convertAndSend("/topic/trip." + ping.getTripId(), ping);

        // Topic 3: Fleet monitoring topic
        messagingTemplate.convertAndSend("/topic/fleet", ping);
    }

    public DriverLocationPing getLatestTripTelemetry(UUID tripId) {
        // 1. Try Redis cache
        try {
            String tripKey = HASH_KEY_TRIP_PREFIX + tripId + ":telemetry";
            Map<Object, Object> data = redisTemplate.opsForHash().entries(tripKey);
            if (!data.isEmpty() && data.containsKey("latitude") && data.containsKey("longitude")) {
                UUID driverId = data.containsKey("driverId") ? UUID.fromString((String) data.get("driverId")) : null;
                double latitude = Double.parseDouble((String) data.get("latitude"));
                double longitude = Double.parseDouble((String) data.get("longitude"));
                double heading = data.containsKey("heading") ? Double.parseDouble((String) data.get("heading")) : 0.0;
                double speedKmph = data.containsKey("speedKmph") ? Double.parseDouble((String) data.get("speedKmph")) : 0.0;
                Double accuracy = data.containsKey("accuracyMeters") ? Double.parseDouble((String) data.get("accuracyMeters")) : null;
                Integer currentStop = data.containsKey("currentStopSequence") ? Integer.parseInt((String) data.get("currentStopSequence")) : null;
                String tsStr = (String) data.get("timestamp");
                Instant timestamp = tsStr != null ? Instant.parse(tsStr) : Instant.now();

                return DriverLocationPing.builder()
                        .driverId(driverId)
                        .tripId(tripId)
                        .latitude(latitude)
                        .longitude(longitude)
                        .heading(heading)
                        .speedKmph(speedKmph)
                        .accuracyMeters(accuracy)
                        .currentStopSequence(currentStop)
                        .timestamp(timestamp)
                        .build();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch telemetry from Redis: {}", e.getMessage());
        }

        // 2. Fallback to Database
        Optional<Trip> tripOpt = tripRepository.findById(tripId);
        if (tripOpt.isPresent()) {
            Trip trip = tripOpt.get();
            if (trip.getLiveLatitude() != null && trip.getLiveLongitude() != null) {
                return DriverLocationPing.builder()
                        .driverId(trip.getDriverId())
                        .tripId(trip.getId())
                        .latitude(trip.getLiveLatitude())
                        .longitude(trip.getLiveLongitude())
                        .heading(trip.getLiveHeading())
                        .speedKmph(trip.getLiveSpeedKmph())
                        .currentStopSequence(trip.getCurrentStopSequence())
                        .timestamp(trip.getLastTelemetryPingAt() != null ? trip.getLastTelemetryPingAt() : Instant.now())
                        .build();
            }
        }

        return null;
    }

    public GeoResults<RedisGeoCommands.GeoLocation<String>> findNearbyDrivers(double latitude, double longitude, double radiusKm) {
        try {
            Circle searchArea = new Circle(new Point(longitude, latitude), new Distance(radiusKm, org.springframework.data.geo.Metrics.KILOMETERS));
            return redisTemplate.opsForGeo().radius(GEO_KEY_ACTIVE_DRIVERS, searchArea);
        } catch (Exception e) {
            log.warn("Error querying nearby drivers from Redis: {}", e.getMessage());
            return null;
        }
    }
}
