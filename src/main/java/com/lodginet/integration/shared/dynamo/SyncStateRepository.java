package com.lodginet.integration.shared.dynamo;

import java.time.Instant;

public interface SyncStateRepository {
    SyncState getState(String entityType);

    void updateLastWebhook(String entityType, Instant timestamp);

    void updateLastPoll(String entityType, Instant timestamp);
}
