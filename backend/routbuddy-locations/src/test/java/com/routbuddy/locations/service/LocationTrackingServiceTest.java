package com.routbuddy.locations.service;

import com.routbuddy.common.domain.event.EventPublisher;
import com.routbuddy.locations.dto.DriverLocationPing;
import com.routbuddy.trips.domain.entity.Trip;
import com.routbuddy.trips.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationTrackingServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private GeoOperations<String, String> geoOperations;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private EventPublisher eventPublisher;

    private LocationTrackingService service;

    private UUID driverId;
    private UUID tripId;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForGeo()).thenReturn(geoOperations);
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);

        service = new LocationTrackingService(redisTemplate, messagingTemplate, tripRepository, eventPublisher);

        driverId = UUID.randomUUID();
        tripId = UUID.randomUUID();
    }

    @Test
    @DisplayName("processLocationPing saves to Redis GEO, Redis Hash and broadcasts over WebSocket")
    void testProcessLocationPing() {
        DriverLocationPing ping = DriverLocationPing.builder()
                .driverId(driverId)
                .tripId(tripId)
                .latitude(28.6139)
                .longitude(77.2090)
                .heading(45.0)
                .speedKmph(35.5)
                .accuracyMeters(5.0)
                .currentStopSequence(2)
                .timestamp(Instant.now())
                .build();

        Trip trip = new Trip();
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        service.processLocationPing(ping);

        // Verify Redis GEO
        verify(geoOperations).add(eq("routbuddy:geo:active_drivers"), eq(new Point(77.2090, 28.6139)), eq(driverId.toString()));

        // Verify Redis Hash
        verify(hashOperations).putAll(eq("routbuddy:trip:" + tripId + ":telemetry"), anyMap());

        // Verify WebSocket broadcasts
        verify(messagingTemplate).convertAndSend(eq("/topic/trips/" + tripId + "/location"), eq(ping));
        verify(messagingTemplate).convertAndSend(eq("/topic/trip." + tripId), eq(ping));
        verify(messagingTemplate).convertAndSend(eq("/topic/fleet"), eq(ping));

        // Verify DB update
        verify(tripRepository).save(trip);
        assertEquals(28.6139, trip.getLiveLatitude());
        assertEquals(77.2090, trip.getLiveLongitude());
    }

    @Test
    @DisplayName("getLatestTripTelemetry reads from Redis Hash when cached")
    void testGetLatestTripTelemetryFromRedis() {
        Map<Object, Object> redisData = new HashMap<>();
        redisData.put("driverId", driverId.toString());
        redisData.put("tripId", tripId.toString());
        redisData.put("latitude", "28.5355");
        redisData.put("longitude", "77.3910");
        redisData.put("heading", "180.0");
        redisData.put("speedKmph", "28.0");
        redisData.put("timestamp", Instant.now().toString());

        when(hashOperations.entries("routbuddy:trip:" + tripId + ":telemetry")).thenReturn(redisData);

        DriverLocationPing result = service.getLatestTripTelemetry(tripId);

        assertNotNull(result);
        assertEquals(28.5355, result.getLatitude());
        assertEquals(77.3910, result.getLongitude());
        assertEquals(180.0, result.getHeading());
        assertEquals(28.0, result.getSpeedKmph());
        assertEquals(driverId, result.getDriverId());
    }
}
