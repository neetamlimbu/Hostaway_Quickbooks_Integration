package com.lodginet.integration.shared.dynamo;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Map;

public class RefreshTokenRepository {

    private final DynamoDbClient dynamo;
    private final String tableName = "QuickBooksTokens";

    public RefreshTokenRepository(DynamoDbClient dynamo) {
        this.dynamo = dynamo;
    }

    public void saveRefreshToken(String refreshToken) {
        dynamo.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(Map.of(
                        "id", AttributeValue.fromS("refreshToken"),
                        "value", AttributeValue.fromS(refreshToken)
                ))
                .build());
    }

    public String loadRefreshToken() {
        var response = dynamo.getItem(GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("id", AttributeValue.fromS("refreshToken")))
                .build());

        if (!response.hasItem()) {
            throw new IllegalStateException("No refresh token stored in DynamoDB");
        }

        return response.item().get("value").s();
    }
}
