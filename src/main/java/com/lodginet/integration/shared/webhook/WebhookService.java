package com.lodginet.integration.shared.webhook;

import com.lodginet.integration.shared.clients.HostawayReservation;
import com.lodginet.integration.shared.clients.QuickBooksObject;
import com.lodginet.integration.shared.clients.SalesReceiptService;
import com.lodginet.integration.shared.dynamo.SyncStateRepository;

import java.time.Instant;
import java.util.Map;

public class WebhookService {

    private final SyncStateRepository syncStateRepo;
    private final SalesReceiptService sales;
    private final HostawayWebhookParser parser;

    public WebhookService(
            SyncStateRepository repo,
            SalesReceiptService sales,
            HostawayWebhookParser parser
    ) {
        this.syncStateRepo = repo;
        this.sales = sales;
        this.parser = parser;
    }

    public void processWebhook(String payload, Map<String, String> headers) {

        HostawayReservation reservation = parser.parseReservation(payload);

        // Update last webhook timestamp
        Instant updated = Instant.ofEpochMilli(reservation.getUpdated());
        syncStateRepo.updateLastWebhook("reservation", updated);

        // Convert → QuickBooksObject
        QuickBooksObject qbObj = QuickBooksObject.fromHostawayEvent(reservation);

        try {
            // Create SalesReceipt in QuickBooks
            var result = sales.createReceipt(qbObj);
            System.out.println("Webhook SalesReceipt created: " + result);

        } catch (Exception e) {
            System.err.println("Failed to process webhook reservation " + reservation.getId() + ": " + e.getMessage());
        }
    }
}
