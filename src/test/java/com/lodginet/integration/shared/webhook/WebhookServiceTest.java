package com.lodginet.integration.shared.webhook;

import com.lodginet.integration.shared.clients.HostawayReservation;
import com.lodginet.integration.shared.clients.QuickBooksObject;
import com.lodginet.integration.shared.clients.SalesReceiptService;
import com.lodginet.integration.shared.dynamo.SyncStateRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class WebhookServiceTest {

    @Test
    public void testProcessWebhook() throws Exception {
        SyncStateRepository repo = mock(SyncStateRepository.class);
        SalesReceiptService sales = mock(SalesReceiptService.class);
        HostawayWebhookParser parser = mock(HostawayWebhookParser.class);

        HostawayReservation reservation = new HostawayReservation();
        reservation.setId(123L);
        reservation.setUpdated(Instant.now().toEpochMilli());
        reservation.setTotalPrice(100.0);
        reservation.setCurrency("USD");
        reservation.setGuestName("John Doe");

        when(parser.parseReservation(anyString())).thenReturn(reservation);

        WebhookService service = new WebhookService(repo, sales, parser);

        service.processWebhook("{}", Map.of());

        verify(parser).parseReservation(anyString());
        verify(repo).updateLastWebhook(eq("reservation"), any());
        verify(sales).createReceipt(any(QuickBooksObject.class));
    }
}
