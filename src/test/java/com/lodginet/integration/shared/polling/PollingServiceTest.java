package com.lodginet.integration.shared.polling;

import com.lodginet.integration.shared.clients.HostawayClient;
import com.lodginet.integration.shared.clients.HostawayReservation;
import com.lodginet.integration.shared.clients.QuickBooksObject;
import com.lodginet.integration.shared.clients.SalesReceiptService;
import com.lodginet.integration.shared.dynamo.SyncState;
import com.lodginet.integration.shared.dynamo.SyncStateRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class PollingServiceTest {

    @Test
    public void testPollingProcessesMissedReservations() throws Exception {
        // Arrange
        HostawayClient hostaway = mock(HostawayClient.class);
        SalesReceiptService sales = mock(SalesReceiptService.class);
        SyncStateRepository repo = mock(SyncStateRepository.class);

        Instant lastPoll = Instant.now().minusSeconds(600);

        // Use the SAME key your production code uses
        SyncState state = new SyncState("reservations", lastPoll, lastPoll);
        when(repo.getState("reservations")).thenReturn(state);

        HostawayReservation r = new HostawayReservation();
        r.setId(999L);
        r.setUpdated(Instant.now().toEpochMilli());

        when(hostaway.getReservationsUpdatedSince(any())).thenReturn(List.of(r));

        PollingService service = new PollingService(hostaway, sales, repo);

        // Act
        service.pollForMissedEvents();

        // Assert
        verify(hostaway).getReservationsUpdatedSince(any());
        verify(sales).createReceipt(any(QuickBooksObject.class));
        verify(repo).updateLastPoll(eq("reservations"), any());
    }
}
