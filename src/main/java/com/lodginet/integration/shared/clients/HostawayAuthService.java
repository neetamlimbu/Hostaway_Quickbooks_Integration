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

        RequestBody body = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .build();

        Request request = new Request.Builder()
                .url("https://api.hostaway.com/v1/accessTokens")
                .post(body)
                .build();

        JsonNode json;
        try (Response response = http.newCall(request).execute()) {

            if (response.body() == null) {
                throw new RuntimeException("Hostaway OAuth failed: empty response body");
            }

            String raw = response.body().string();
            json = mapper.readTree(raw);
        }

        // Hostaway returns access_token at the top level
        String token = json.path("access_token").asText(null);
        long expiresIn = json.path("expires_in").asLong(3600);

        if (token == null) {
            throw new RuntimeException("Hostaway OAuth error: " + json.toString());
        }

        this.cachedToken = token;
        this.expiresAt = now + (expiresIn * 1000);

        return token;
    }
}
