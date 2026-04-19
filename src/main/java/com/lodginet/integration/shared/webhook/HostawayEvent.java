package com.lodginet.integration.shared.webhook;

import java.time.Instant;

public class HostawayEvent {
    private final String entityType;
    private final Instant timestamp;
    private final Object payload;

    public HostawayEvent(String entityType, Instant timestamp, Object payload) {
        this.entityType = entityType;
        this.timestamp = timestamp;
        this.payload = payload;
    }

    public String getEntityType() {
        return entityType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Object getPayload() {
        return payload;
    }
}
