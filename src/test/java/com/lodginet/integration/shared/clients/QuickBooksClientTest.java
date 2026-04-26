package com.lodginet.integration.shared.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lodginet.integration.shared.dynamo.RefreshTokenRepository;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import com.lodginet.integration.oauth.OAuthService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class QuickBooksClientTest {

    @Test
    void testCreateSalesReceiptWithTokenRefresh() throws Exception {

        try (MockWebServer server = new MockWebServer()) {
            server.start();

            String baseUrl = server.url("").toString();

            ObjectMapper mapper = new ObjectMapper();
            OkHttpClient http = new OkHttpClient();

            OAuthService oauth = mock(OAuthService.class);
            RefreshTokenRepository repo = mock(RefreshTokenRepository.class);

            // 1) First call → 401
            server.enqueue(new MockResponse().setResponseCode(401));

            // 2) OAuth refreshAccessToken() returns new tokens
            when(oauth.refreshAccessToken()).thenReturn(
                    mapper.readTree("""
                                {
                                  "access_token": "NEW_ACCESS",
                                  "refresh_token": "NEW_REFRESH"
                                }
                            """)
            );

            // 3) After refresh, second call → 200
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("""
                                {
                                  "SalesReceipt": { "Id": "123" }
                                }
                            """));

            when(repo.loadRefreshToken()).thenReturn("OLD_REFRESH");

            QuickBooksClient client = new QuickBooksClient(
                    http,
                    mapper,
                    oauth,
                    repo,
                    "12345",
                    baseUrl
            );

            JsonNode result = client.createSalesReceipt("{\"dummy\":true}");

            assertEquals("123", result.get("SalesReceipt").get("Id").asText());

            verify(oauth, times(2)).refreshAccessToken();
            verify(repo, times(2)).saveRefreshToken("NEW_REFRESH");
            assertEquals(2, server.getRequestCount());
        }
    }
}
