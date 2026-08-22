package com.routbuddy.common.domain.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public abstract class DomainEvent implements Serializable {
    private final UUID eventId;
    private final Instant occurredAt;
    private final String eventType;

    protected DomainEvent(String eventType) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.eventType = eventType;
    }

    public UUID getEventId() {
        return eventId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getEventType() {
        return eventType;
    }
}
