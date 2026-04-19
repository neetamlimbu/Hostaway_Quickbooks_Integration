package com.lodginet.integration.shared.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lodginet.integration.shared.clients.HostawayReservation;

public class HostawayWebhookParser {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Parse the webhook JSON payload into a HostawayReservation.
     */
    public HostawayReservation parseReservation(String payload) {
        try {
            return mapper.readValue(payload, HostawayReservation.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Hostaway webhook payload", e);
        }
    }
}