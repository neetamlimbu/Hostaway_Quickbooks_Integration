package com.lodginet.integration.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class OAuthService {

    private static final String TOKEN_URL = "https://oauth.platform.intuit.com/oauth2/v1/tokens/bearer";
    public static final ObjectMapper mapper = new ObjectMapper();
    private final OkHttpClient client = new OkHttpClient();

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    // Stored tokens
    private String accessToken;
    private String refreshToken;

    public OAuthService(String clientId, String clientSecret, String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    // STEP 1 — Build login URL
    public String buildLoginUrl() {
        return "https://appcenter.intuit.com/connect/oauth2"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&scope=com.intuit.quickbooks.accounting"
                + "&state=12345";
    }

    // STEP 2 — Exchange authorization code for tokens
    public JsonNode exchangeAuthorizationCode(String code) throws Exception {
        String authHeader = basicAuthHeader();

        RequestBody body = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", redirectUri)
                .build();

        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .post(body)
                .addHeader("Authorization", authHeader)
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            JsonNode json = mapper.readTree(response.body().string());

            this.accessToken = json.path("access_token").asText(null);
            this.refreshToken = json.path("refresh_token").asText(null);

            return json;
        }
    }

    // STEP 3 — Refresh token
    public JsonNode refreshAccessToken(String refreshToken) throws Exception {
        String authHeader = basicAuthHeader();

        RequestBody body = new FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .build();

        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .post(body)
                .addHeader("Authorization", authHeader)
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
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

    // STEP 4 — Return a valid access token (refresh if needed)
    public String getValidAccessToken() {
        return accessToken;
    }

    // STEP 5 — Return stored refresh token
    public String getRefreshToken() {
        return refreshToken;
    }

    public void refreshToken() throws Exception {
        JsonNode json = refreshAccessToken(this.refreshToken);

        JsonNode accessNode = json.path("access_token");
        JsonNode refreshNode = json.path("refresh_token");

        this.accessToken = accessNode.isMissingNode() || accessNode.isNull()
                ? null
                : accessNode.asText();

        this.refreshToken = refreshNode.isMissingNode() || refreshNode.isNull()
                ? null
                : refreshNode.asText();
    }

    public JsonNode parseJson(String json) throws Exception {
        return mapper.readTree(json);
    }
}