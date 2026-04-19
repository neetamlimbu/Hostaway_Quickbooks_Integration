package com.lodginet.integration.shared.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

public class HostawayAuthService {

    private final OkHttpClient http = new OkHttpClient();
    private final ObjectMapper mapper;
    private final String clientId;
    private final String clientSecret;

    private String cachedToken;
    private long expiresAt;

    public HostawayAuthService(ObjectMapper mapper, String clientId, String clientSecret) {
        this.mapper = mapper;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public synchronized String getAccessToken() throws Exception {
        long now = System.currentTimeMillis();

        if (cachedToken != null && now < expiresAt) {
            return cachedToken;
        }

        RequestBody body = RequestBody.create(
                "{ \"grant_type\": \"client_credentials\", " +
                        "\"client_id\": \"" + clientId + "\", " +
                        "\"client_secret\": \"" + clientSecret + "\" }",
                MediaType.get("application/json")
        );

        Request request = new Request.Builder()
                .url("https://api.hostaway.com/v1/accessTokens")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        JsonNode json;
        try (Response response = http.newCall(request).execute()) {
            assert response.body() != null;
            json = mapper.readTree(response.body().string());
        }

        String token = json.get("result").get("access_token").asText();
        long expiresIn = json.get("result").get("expires_in").asLong();

        this.cachedToken = token;
        this.expiresAt = now + (expiresIn * 1000);

        return token;
    }
}
