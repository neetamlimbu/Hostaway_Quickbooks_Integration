package com.lodginet.integration.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class OAuthService {

    public static final ObjectMapper mapper = new ObjectMapper();
    private final OkHttpClient client = new OkHttpClient();

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String tokenEndpoint;

    private String accessToken;
    private String refreshToken;

    public OAuthService(
            String clientId,
            String clientSecret,
            String redirectUri,
            String refreshToken,
            String tokenEndpoint
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.refreshToken = refreshToken;
        this.tokenEndpoint = tokenEndpoint;
    }

    public JsonNode exchangeAuthorizationCode(String code) throws Exception {
        String authHeader = basicAuthHeader();

        RequestBody body = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", redirectUri)
                .build();

        Request request = new Request.Builder()
                .url(tokenEndpoint)
                .post(body)
                .addHeader("Authorization", authHeader)
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            JsonNode json = mapper.readTree(response.body().string());
            this.accessToken = json.path("access_token").asText(null);
            this.refreshToken = json.path("refresh_token").asText(null);
            return json;
        }
    }

    public JsonNode refreshAccessToken() throws Exception {
        String authHeader = basicAuthHeader();

        RequestBody body = new FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .build();

        Request request = new Request.Builder()
                .url(tokenEndpoint)
                .post(body)
                .addHeader("Authorization", authHeader)
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            JsonNode json = mapper.readTree(response.body().string());
            this.accessToken = json.path("access_token").asText(null);
            this.refreshToken = json.path("refresh_token").asText(null);
            return json;
        }
    }

    private String basicAuthHeader() {
        String raw = clientId + ":" + clientSecret;
        String encoded = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    public String getValidAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
