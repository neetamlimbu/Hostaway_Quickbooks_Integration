package com.lodginet.integration.shared.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HostawayClientIT {

    @Test
    void getsListingsWithPaginationAndRetry() throws Exception {

        try (MockWebServer server = new MockWebServer()) {

            // Page 1 — success
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("""
                            {
                              "result": {
                                "listings": [
                                  { "id": 1, "name": "A", "updated": 1712345678000 }
                                ],
                                "nextPage": 2
                              }
                            }
                            """)
                    .addHeader("Content-Type", "application/json"));

            // Page 2 — transient 500 (must still be JSON!)
            server.enqueue(new MockResponse()
                    .setResponseCode(500)
                    .setBody("""
                            {
                              "error": "server error"
                            }
                            """)
                    .addHeader("Content-Type", "application/json"));

            // Page 2 retry — success
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("""
                            {
                              "result": {
                                "listings": [
                                  { "id": 2, "name": "B", "updated": 1712345678000 }
                                ],
                                "nextPage": null
                              }
                            }
                            """)
                    .addHeader("Content-Type", "application/json"));

            server.start();

            String baseUrl = server.url("/").toString();

            // Mock HostawayAuthService
            HostawayAuthService auth = mock(HostawayAuthService.class);
            when(auth.getAccessToken()).thenReturn("FAKE_TOKEN");

            HostawayClient client = new HostawayClient(
                    new OkHttpClient(),
                    new ObjectMapper(),
                    auth,
                    baseUrl
            );

            List<HostawayListing> list =
                    client.getListingsUpdatedSince(Instant.parse("2024-01-01T00:00:00Z"));

            assertEquals(2, list.size());
            assertEquals(1L, list.get(0).getId());
            assertEquals(2L, list.get(1).getId());
        }
    }
}
