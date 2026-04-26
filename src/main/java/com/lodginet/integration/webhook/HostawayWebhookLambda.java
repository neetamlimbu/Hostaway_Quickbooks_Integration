package com.lodginet.integration.webhook;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lodginet.integration.config.Settings;
import com.lodginet.integration.shared.clients.HostawayAuthService;
import com.lodginet.integration.shared.clients.HostawayClient;
import okhttp3.OkHttpClient;

import java.time.Instant;

public class HostawayWebhookLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ObjectMapper mapper;
    private final HostawayClient hostawayClient;

    // ----------------------------------------------------------------------
    // PRODUCTION CONSTRUCTOR — uses Settings + real dependencies
    // ----------------------------------------------------------------------
    public HostawayWebhookLambda() {
        ObjectMapper mapper = new ObjectMapper();

        HostawayAuthService auth = new HostawayAuthService(
                mapper,
                Settings.hostawayClientId(),
                Settings.hostawayClientSecret()
        );

        HostawayClient client = new HostawayClient(
                new OkHttpClient(),
                mapper,
                auth,
                Settings.hostawayBaseUrl()
        );

        this.mapper = mapper;
        this.hostawayClient = client;
    }

    // ----------------------------------------------------------------------
    // TEST CONSTRUCTOR — allows injecting a mock HostawayClient
    // ----------------------------------------------------------------------
    public HostawayWebhookLambda(HostawayClient hostawayClient) {
        this.mapper = new ObjectMapper();
        this.hostawayClient = hostawayClient;
    }

    // ----------------------------------------------------------------------
    // LAMBDA HANDLER
    // ----------------------------------------------------------------------
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            JsonNode event = mapper.readTree(input.getBody());
            String eventType = event.path("event").asText("unknown");

            log(context, "Received Hostaway webhook event: " + eventType);

            return switch (eventType) {
                case "reservation_created", "reservation_updated" -> handleReservationWebhook(event, context);

                case "new_message_received" -> ok("message webhook processed");

                default -> {
                    log(context, "Ignoring unsupported event type: " + eventType);
                    yield ok("ignored");
                }
            };

        } catch (Exception e) {
            log(context, "Error processing webhook: " + e.getMessage());
            return error("error processing webhook");
        }
    }

    // ----------------------------------------------------------------------
    // EVENT HANDLERS
    // ----------------------------------------------------------------------
    private APIGatewayProxyResponseEvent handleReservationWebhook(JsonNode event, Context context) {
        try {
            JsonNode reservation = event.path("data").path("reservation");
            long reservationId = reservation.path("id").asLong();

            log(context, "Processing reservation webhook for ID: " + reservationId);

            if (reservationId == 0) {
                log(context, "Invalid webhook payload: missing reservation.id");
                return ok("ignored");
            }

            hostawayClient.getReservationsUpdatedSince(
                    Instant.now().minusSeconds(86400)
            );

            return ok("ok");

        } catch (Exception e) {
            log(context, "Error handling reservation webhook: " + e.getMessage());
            return error("error handling reservation webhook");
        }
    }

    private APIGatewayProxyResponseEvent handleMessageWebhook(JsonNode event, Context context) {
        log(context, "Message webhook received");
        return ok("ok");
    }

    // ----------------------------------------------------------------------
    // RESPONSE HELPERS
    // ----------------------------------------------------------------------
    private APIGatewayProxyResponseEvent ok(String body) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(body);
    }

    private APIGatewayProxyResponseEvent error(String body) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(500)
                .withBody(body);
    }

    // ----------------------------------------------------------------------
    // LOGGING
    // ----------------------------------------------------------------------
    private void log(Context context, String msg) {
        if (context != null && context.getLogger() != null) {
            context.getLogger().log(msg + "\n");
        }
    }
}
