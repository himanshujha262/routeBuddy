package com.routbuddy.locations.listener;

import com.routbuddy.common.domain.event.TripOccupancyChangedEvent;
import com.routbuddy.locations.service.TripOccupancyBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TripOccupancyEventListener {

    private final TripOccupancyBroadcaster tripOccupancyBroadcaster;

    @EventListener
    public void handleOccupancyChanged(TripOccupancyChangedEvent event) {
        log.info("Received TripOccupancyChangedEvent for trip {}: available={}/{}",
                event.getTripId(), event.getAvailableSeats(), event.getTotalSeats());

        tripOccupancyBroadcaster.broadcastOccupancy(
                event.getTripId(),
                event.getTotalSeats(),
                event.getAvailableSeats(),
                event.getStatus()
        );
    }
}
