package com.lodginet.integration.shared.polling;

import com.lodginet.integration.shared.clients.HostawayClient;
import com.lodginet.integration.shared.clients.HostawayReservation;
import com.lodginet.integration.shared.clients.QuickBooksObject;
import com.lodginet.integration.shared.clients.SalesReceiptService;
import com.lodginet.integration.shared.dynamo.SyncState;
import com.lodginet.integration.shared.dynamo.SyncStateRepository;

import java.time.Instant;
import java.util.List;

public class PollingService {

    private final HostawayClient hostaway;
    private final SalesReceiptService sales;
    private final SyncStateRepository syncRepo;

    public PollingService(
            HostawayClient hostaway,
            SalesReceiptService sales,
            SyncStateRepository syncRepo
    ) {
        this.hostaway = hostaway;
        this.sales = sales;
        this.syncRepo = syncRepo;
    }

    public void pollForMissedEvents() {

        SyncState state = syncRepo.getState("reservations");

        List<HostawayReservation> events = null;
        try {
            events = hostaway.getReservationsUpdatedSince(
                    state.getLastPollTimestamp()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (var event : events) {

            QuickBooksObject qbObj = QuickBooksObject.fromHostawayEvent(event);

            try {
                var result = sales.createReceipt(qbObj);
                System.out.println("Created SalesReceipt: " + result);

                syncRepo.updateLastPoll("reservations", Instant.now());

            } catch (Exception ex) {
                System.err.println("Failed to sync event " + event.getId() + ": " + ex.getMessage());
            }
        }
    }
}
