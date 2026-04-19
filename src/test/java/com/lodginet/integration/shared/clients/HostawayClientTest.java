package com.lodginet.integration.shared.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HostawayClientTest {

    @Test
    public void testGetReservationsUpdatedSince() throws Exception {
        // Arrange
        OkHttpClient http = mock(OkHttpClient.class);
        Call call = mock(Call.class);
        Response response = mock(Response.class);
        ResponseBody body = mock(ResponseBody.class);

        String json = """
                {
                  "result": {
                    "reservations": [
                      { "id": 1, "guestName": "John", "updated": 1712345678000 }
                    ]
                  }
                }
                """;

        when(body.string()).thenReturn(json);

        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url("https://dummy.hostaway.test").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(body)
                .build();

        when(http.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(mockResponse);

        // Mock HostawayAuthService
        HostawayAuthService auth = mock(HostawayAuthService.class);
        when(auth.getAccessToken()).thenReturn("FAKE_TOKEN");

        HostawayClient client = new HostawayClient(
                http,
                new ObjectMapper(),
                auth,
                "https://dummy.hostaway.test"
        );

        // Act
        List<HostawayReservation> list =
                client.getReservationsUpdatedSince(Instant.now().minusSeconds(3600));

        // Assert
        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).getId());
        assertEquals("John", list.get(0).getGuestName());
        assertEquals(1712345678000L, list.get(0).getUpdated());
    }
}
