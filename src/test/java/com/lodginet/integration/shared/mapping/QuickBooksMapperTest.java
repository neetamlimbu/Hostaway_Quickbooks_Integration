package com.lodginet.integration.shared.mapping;

import com.lodginet.integration.shared.clients.HostawayReservation;
import com.lodginet.integration.shared.clients.QuickBooksObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QuickBooksMapperTest {

    @Test
    public void testMapReservationToQuickBooksObject() {
        HostawayReservation r = new HostawayReservation();
        r.setId(1L);
        r.setGuestName("Alice");
        r.setGuestEmail("alice@example.com");
        r.setTotalPrice(200.0);
        r.setCurrency("GBP");

        QuickBooksObject qb = QuickBooksMapper.map(r);

        assertEquals("Alice", qb.getCustomerName());
        assertEquals("alice@example.com", qb.getCustomerEmail());
        assertEquals(200.0, qb.getAmount());
        assertEquals("GBP", qb.getCurrency());
        assertEquals(1L, qb.getHostawayReservationId());
    }
}