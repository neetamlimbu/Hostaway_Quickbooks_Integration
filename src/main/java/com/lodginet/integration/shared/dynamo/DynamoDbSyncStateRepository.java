package com.lodginet.integration.shared.dynamo;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.Map;

public class DynamoDbSyncStateRepository implements SyncStateRepository {

    private final DynamoDbClient dynamo;
    private final String tableName;

    public DynamoDbSyncStateRepository(DynamoDbClient dynamo, String tableName) {
        this.dynamo = dynamo;
        this.tableName = tableName;
    }

    @Override
    public SyncState getState(String entityType) {
        GetItemResponse response = dynamo.getItem(GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("entityType", AttributeValue.fromS(entityType)))
                .build());

        if (!response.hasItem()) {
            return new SyncState(entityType, Instant.EPOCH, Instant.EPOCH);
        }

        var item = response.item();

        Instant lastWebhook = item.containsKey("lastWebhook")
                ? Instant.parse(item.get("lastWebhook").s())
                : Instant.EPOCH;

        Instant lastPoll = item.containsKey("lastPoll")
                ? Instant.parse(item.get("lastPoll").s())
                : Instant.EPOCH;

        return new SyncState(entityType, lastWebhook, lastPoll);
    }

    @Override
    public void updateLastWebhook(String entityType, Instant timestamp) {
        dynamo.updateItem(UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("entityType", AttributeValue.fromS(entityType)))
                .updateExpression("SET lastWebhook = :ts")
                .expressionAttributeValues(Map.of(
                        ":ts", AttributeValue.fromS(timestamp.toString())
                ))
                .build());
    }

    @Override
    public void updateLastPoll(String entityType, Instant timestamp) {
        dynamo.updateItem(UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("entityType", AttributeValue.fromS(entityType)))
                .updateExpression("SET lastPoll = :ts")
                .expressionAttributeValues(Map.of(
                        ":ts", AttributeValue.fromS(timestamp.toString())
                ))
                .build());
    }
}
