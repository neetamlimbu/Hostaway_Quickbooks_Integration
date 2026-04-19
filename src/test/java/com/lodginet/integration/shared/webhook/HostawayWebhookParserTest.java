package com.lodginet.integration.shared.webhook;

import com.lodginet.integration.shared.clients.HostawayReservation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HostawayWebhookParserTest {

    @Test
    public void testParseReservation() {
        String json = """
                {
                  "id": 12345,
                  "listingId": 678,
                  "guestName": "John Doe",
                  "guestEmail": "john@example.com",
                  "guestPhone": "+123456789",
                  "checkIn": "2024-05-01",
                  "checkOut": "2024-05-05",
                  "totalPrice": 450.00,
                  "currency": "USD",
                  "updated": 1712345678000
                }
                """;

        HostawayWebhookParser parser = new HostawayWebhookParser();
        HostawayReservation r = parser.parseReservation(json);

        assertEquals(12345L, r.getId());
        assertEquals("John Doe", r.getGuestName());
        assertEquals("USD", r.getCurrency());
        assertEquals("2024-05-01", r.getCheckIn());
    }
}