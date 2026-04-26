package com.lodginet.integration.shared.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lodginet.integration.oauth.OAuthService;
import com.lodginet.integration.shared.dynamo.RefreshTokenRepository;
import okhttp3.*;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

public class QuickBooksClient {

    private static final MediaType JSON = MediaType.get("application/json");

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final OAuthService oauth;
    private final RefreshTokenRepository tokenRepo;
    private final String realmId;
    private final String baseUrl;

    public QuickBooksClient(
            OkHttpClient httpClient,
            ObjectMapper objectMapper,
            OAuthService oauth,
            RefreshTokenRepository tokenRepo,
            String realmId,
            String baseUrl
    ) {
        this.httpClient = Objects.requireNonNull(httpClient);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.oauth = Objects.requireNonNull(oauth);
        this.tokenRepo = Objects.requireNonNull(tokenRepo);
        this.realmId = Objects.requireNonNull(realmId);
        this.baseUrl = Objects.requireNonNull(baseUrl);
    }

    // ----------------------------------------------------------------------
    // PUBLIC API
    // ----------------------------------------------------------------------
    public JsonNode createSalesReceipt(String salesReceiptJson) throws Exception {
        return executeWithAuth(accessToken -> {
            String url = baseUrl + "/v3/company/" + realmId + "/salesreceipt?minorversion=70";

            RequestBody body = RequestBody.create(salesReceiptJson, JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Accept", "application/json")
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {

                if (response.code() == 401) {
                    throw new QuickBooksUnauthorizedException("401 Unauthorized from QuickBooks");
                }

                if (!response.isSuccessful()) {
                    throw new IllegalStateException("QuickBooks error: HTTP " + response.code());
                }

                assert response.body() != null;
                return objectMapper.readTree(response.body().string());

            } catch (IOException e) {
                throw new RuntimeException("QuickBooks request failed", e);
            }
        });
    }

    // ----------------------------------------------------------------------
    // TOKEN HANDLING + RETRY LOGIC
    // ----------------------------------------------------------------------
    private <T> T executeWithAuth(Function<String, T> action) throws Exception {
        // 1. Ensure we have a refresh token stored
        ensureRefreshTokenExists();

        // 2. Always refresh before the call (QuickBooks tokens are short-lived)
        refreshAccessTokenAndPersist();

        String accessToken = oauth.getValidAccessToken();

        try {
            return action.apply(accessToken);

        } catch (QuickBooksUnauthorizedException e) {
            // 3. Retry once after refreshing again
            refreshAccessTokenAndPersist();
            String newToken = oauth.getValidAccessToken();
            return action.apply(newToken);
        }
    }

    private void ensureRefreshTokenExists() {
        String stored = tokenRepo.loadRefreshToken();
        if (stored == null || stored.isBlank()) {
            throw new IllegalStateException("No QuickBooks refresh token stored in DynamoDB");
        }
    }

    private void refreshAccessTokenAndPersist() throws Exception {
        JsonNode json = oauth.refreshAccessToken();

        if (json == null) {
            throw new IllegalStateException("OAuthService returned null during refresh");
        }

        String newRefresh = json.path("refresh_token").asText(null);
        if (newRefresh != null && !newRefresh.isBlank()) {
            tokenRepo.saveRefreshToken(newRefresh);
        }
    }

    // ----------------------------------------------------------------------
    // EXCEPTION
    // ----------------------------------------------------------------------
    public static class QuickBooksUnauthorizedException extends RuntimeException {
        public QuickBooksUnauthorizedException(String msg) {
            super(msg);
        }
    }
}
