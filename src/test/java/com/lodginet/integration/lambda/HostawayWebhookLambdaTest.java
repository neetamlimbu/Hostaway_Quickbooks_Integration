package com.lodginet.integration.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.lodginet.integration.shared.clients.HostawayClient;
import com.lodginet.integration.webhook.HostawayWebhookLambda;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class HostawayWebhookLambdaTest {

    @Test
    void testLambdaHandler() throws Exception {
        // Mock HostawayClient so no real HTTP or env vars are needed
        HostawayClient hostawayClient = mock(HostawayClient.class);

        Context ctx = mock(Context.class);
        LambdaLogger logger = mock(LambdaLogger.class);
        when(ctx.getLogger()).thenReturn(logger);

        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setBody("{\"event\":\"reservation_created\",\"reservationId\":123}");

        // Use test constructor
        HostawayWebhookLambda lambda = new HostawayWebhookLambda(hostawayClient);

        String result = lambda.handleRequest(event, ctx);

        assertEquals("ok", result);

        // Verify we attempted to fetch reservations
        verify(hostawayClient, times(1)).getReservationsUpdatedSince(any(Instant.class));
    }
}
