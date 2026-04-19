package com.lodginet.integration.shared.state;

import com.lodginet.integration.shared.dynamo.DynamoDbSyncStateRepository;
import com.lodginet.integration.shared.dynamo.SyncState;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DynamoDbSyncStateRepositoryTest {

    @Test
    void testGetStateWhenItemMissingReturnsEpoch() {
        DynamoDbClient dynamo = mock(DynamoDbClient.class);

        when(dynamo.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder().item(Map.of()).build());

        DynamoDbSyncStateRepository repo =
                new DynamoDbSyncStateRepository(dynamo, "SyncStateTable");

        SyncState state = repo.getState("reservation");

        assert state.getLastPollTimestamp().equals(Instant.EPOCH);
        assert state.getLastWebhookTimestamp().equals(Instant.EPOCH);
    }

    @Test
    void testUpdateLastWebhookUsesUpdateItem() {
        DynamoDbClient dynamo = mock(DynamoDbClient.class);

        DynamoDbSyncStateRepository repo =
                new DynamoDbSyncStateRepository(dynamo, "SyncStateTable");

        repo.updateLastWebhook("reservation", Instant.parse("2026-04-18T19:49:58.047661200Z"));

        verify(dynamo).updateItem(any(UpdateItemRequest.class));
        verify(dynamo, never()).putItem(any(PutItemRequest.class));
    }
}
