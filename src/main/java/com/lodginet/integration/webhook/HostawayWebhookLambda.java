package com.lodginet.integration.webhook;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lodginet.integration.config.Settings;
import com.lodginet.integration.shared.clients.HostawayAuthService;
import com.lodginet.integration.shared.clients.HostawayClient;
import okhttp3.OkHttpClient;

import java.time.Instant;

public class HostawayWebhookLambda implements RequestHandler<APIGatewayProxyRequestEvent, String> {

    private final ObjectMapper mapper;
    private final HostawayClient hostawayClient;

    /**
     * Production constructor – wires real dependencies using Settings/env.
     */
    public HostawayWebhookLambda() {
        this(new HostawayClient(
                new OkHttpClient(),
                new ObjectMapper(),
                new HostawayAuthService(
                        new ObjectMapper(),
                        Settings.hostawayClientId(),
                        Settings.hostawayClientSecret()
                ),
                Settings.hostawayBaseUrl()
        ));
    }

    /**
     * Test constructor – allows injecting a mock HostawayClient.
     */
    public HostawayWebhookLambda(HostawayClient hostawayClient) {
        this.mapper = new ObjectMapper();
        this.hostawayClient = hostawayClient;
    }

    @Override
    public String handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            JsonNode event = mapper.readTree(input.getBody());

            String eventType = event.path("event").asText();
            log(context, "Received Hostaway webhook event: " + eventType);

            return switch (eventType) {
                case "reservation_created", "reservation_updated" -> handleReservationWebhook(event, context);
                case "new_message_received" -> handleMessageWebhook(event, context);
                default -> "ignored";
            };

        } catch (Exception e) {
            log(context, "Error processing webhook: " + e.getMessage());
            return "error";
        }
    }

    private String handleReservationWebhook(JsonNode event, Context context) {
        try {
            long reservationId = event.path("reservationId").asLong();
            log(context, "Fetching reservation: " + reservationId);

            // In tests this is a mock, so no real HTTP happens.
            hostawayClient.getReservationsUpdatedSince(
                    Instant.now().minusSeconds(86400)
            );

            return "ok";

        } catch (Exception e) {
            log(context, "Error handling reservation webhook: " + e.getMessage());
            return "error";
        }
    }

    private String handleMessageWebhook(JsonNode event, Context context) {
        log(context, "Message webhook received");
        return "ok";
    }

    private void log(Context context, String msg) {
        if (context != null && context.getLogger() != null) {
            context.getLogger().log(msg);
        }
    }
}
