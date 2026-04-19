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
    private final OAuthService oAuthService; // <-- your real class
    private final RefreshTokenRepository refreshTokenRepository;
    private final String baseUrl;
    private final String realmId;

    public QuickBooksClient(OkHttpClient httpClient,
                            ObjectMapper objectMapper,
                            OAuthService oAuthService,
                            RefreshTokenRepository refreshTokenRepository,
                            String realmId,
                            String baseUrl) {

        this.httpClient = Objects.requireNonNull(httpClient);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.oAuthService = Objects.requireNonNull(oAuthService);
        this.refreshTokenRepository = Objects.requireNonNull(refreshTokenRepository);
        this.realmId = Objects.requireNonNull(realmId);
        this.baseUrl = Objects.requireNonNull(baseUrl);
    }

    // -----------------------------
    // PUBLIC API
    // -----------------------------
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
                    throw new QuickBooksUnauthorizedException("401 from QuickBooks");
                }
                if (!response.isSuccessful()) {
                    throw new IllegalStateException("HTTP " + response.code());
                }
                return objectMapper.readTree(response.body().string());
            } catch (IOException e) {
                throw new RuntimeException("QuickBooks call failed", e);
            }
        });
    }

    // -----------------------------
    // TOKEN HANDLING
    // -----------------------------
    private <T> T executeWithAuth(Function<String, T> action) throws Exception {
        String token = getAccessToken();

        try {
            return action.apply(token);
        } catch (QuickBooksUnauthorizedException e) {
            // refresh and retry once
            oAuthService.refreshToken();
            refreshTokenRepository.saveRefreshToken(oAuthService.getRefreshToken());
            String newToken = oAuthService.getValidAccessToken();
            return action.apply(newToken);
        }
    }

    private String getAccessToken() throws Exception {
        String storedRefresh = refreshTokenRepository.loadRefreshToken();
        if (storedRefresh == null || storedRefresh.isBlank()) {
            throw new IllegalStateException("No refresh token stored");
        }

        JsonNode json = oAuthService.refreshAccessToken(storedRefresh);
        if (json == null) {
            throw new IllegalStateException("OAuthService returned null");
        }

        String access = json.path("access_token").asText(null);
        if (access == null) {
            throw new IllegalStateException("Missing access_token");
        }

        String newRefresh = json.path("refresh_token").asText(null);
        if (newRefresh != null) {
            refreshTokenRepository.saveRefreshToken(newRefresh);
        }

        return access;
    }

    // -----------------------------
    // EXCEPTION
    // -----------------------------
    public static class QuickBooksUnauthorizedException extends RuntimeException {
        public QuickBooksUnauthorizedException(String msg) {
            super(msg);
        }
    }
}
