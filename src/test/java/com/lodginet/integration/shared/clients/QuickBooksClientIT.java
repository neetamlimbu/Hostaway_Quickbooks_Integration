package com.lodginet.integration.shared.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lodginet.integration.oauth.OAuthService;
import com.lodginet.integration.shared.dynamo.RefreshTokenRepository;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class QuickBooksClientIT {

    @Test
    void createSalesReceipt_withTokenRefresh() throws Exception {

        try (MockWebServer apiServer = new MockWebServer()) {

            // 1st call → 401 (expired/invalid token)
            apiServer.enqueue(new MockResponse()
                    .setResponseCode(401)
                    .setBody("""
                            {
                              "Fault": {
                                "Error": [
                                  { "Message": "Token expired" }
                                ]
                              }
                            }
                            """)
                    .addHeader("Content-Type", "application/json"));

            // 2nd call → success with SalesReceipt
            apiServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("""
                            {
                              "SalesReceipt": { "Id": "123" }
                            }
                            """)
                    .addHeader("Content-Type", "application/json"));

            apiServer.start();

            String apiBase = apiServer.url("/v3/company").toString();

            ObjectMapper mapper = new ObjectMapper();

            // --- Mock RefreshTokenRepository ---
            RefreshTokenRepository repo = mock(RefreshTokenRepository.class);
            when(repo.loadRefreshToken()).thenReturn("OLD_REFRESH");

            // --- Mock OAuthService.refreshAccessToken(refreshToken) ---
            OAuthService oauth = mock(OAuthService.class);
            JsonNode tokenResponse = mapper.readTree("""
                    {
                      "access_token": "NEW_ACCESS",
                      "refresh_token": "NEW_REFRESH"
                    }
                    """);
            when(oauth.refreshAccessToken()).thenReturn(tokenResponse);

            OkHttpClient http = new OkHttpClient();

            QuickBooksClient client = new QuickBooksClient(
                    http,
                    mapper,
                    oauth,
                    repo,
                    "1234567890",
                    apiBase
            );

            // Act
            JsonNode result = client.createSalesReceipt("{\"dummy\":true}");

            // Assert
            assertEquals("123", result.get("SalesReceipt").get("Id").asText());

            // Verify refresh flow
            verify(repo, times(1)).loadRefreshToken();
            verify(oauth, times(2)).refreshAccessToken();
            verify(repo, times(2)).saveRefreshToken("NEW_REFRESH");
        }
    }
}
