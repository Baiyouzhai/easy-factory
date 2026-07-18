package com.byz.factory.event;

import java.time.Instant;
import java.util.UUID;

/**
 * 领域事件简单实现
 *
 * @author 苏政
 */
class SimpleDomainEvent implements IDomainEvent {
    private final String eventId;
    private final String eventType;
    private final Instant timestamp;
    private final String source;
    private final Object payload;

    SimpleDomainEvent(String eventType, String source, Object payload) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.timestamp = Instant.now();
        this.source = source;
        this.payload = payload;
    }

    @Override
    public String getEventId() { return eventId; }

    @Override
    public String getEventType() { return eventType; }

    @Override
    public Instant getTimestamp() { return timestamp; }

    @Override
    public String getSource() { return source; }

    @Override
    public Object getPayload() { return payload; }
}
