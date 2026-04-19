package com.lodginet.integration.shared.dynamo;

import java.time.Instant;

public class SyncState {

    private final String entityType;
    private Instant lastWebhookTimestamp;
    private Instant lastPollTimestamp;

    public SyncState(String entityType, Instant lastWebhookTimestamp, Instant lastPollTimestamp) {
        this.entityType = entityType;
        this.lastWebhookTimestamp = lastWebhookTimestamp;
        this.lastPollTimestamp = lastPollTimestamp;
    }

    public String getEntityType() {
        return entityType;
    }

    public Instant getLastWebhookTimestamp() {
        return lastWebhookTimestamp;
    }

    public Instant getLastPollTimestamp() {
        return lastPollTimestamp;
    }

    public void setLastWebhookTimestamp(Instant ts) {
        this.lastWebhookTimestamp = ts;
    }

    public void setLastPollTimestamp(Instant ts) {
        this.lastPollTimestamp = ts;
    }
}
