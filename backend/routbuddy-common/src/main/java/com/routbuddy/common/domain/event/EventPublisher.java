package com.routbuddy.common.domain.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);
    private final ApplicationEventPublisher applicationEventPublisher;

    public EventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publish(DomainEvent event) {
        log.info("Publishing domain event: [type={}, id={}, occurredAt={}]",
                event.getEventType(), event.getEventId(), event.getOccurredAt());
        applicationEventPublisher.publishEvent(event);
    }
}
