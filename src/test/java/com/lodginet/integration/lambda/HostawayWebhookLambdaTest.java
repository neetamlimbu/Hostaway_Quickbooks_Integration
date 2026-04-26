package com.lodginet.integration.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
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
        HostawayClient hostawayClient = mock(HostawayClient.class);

        Context ctx = mock(Context.class);
        LambdaLogger logger = mock(LambdaLogger.class);
        when(ctx.getLogger()).thenReturn(logger);

        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setBody(
                "{\"event\":\"reservation_created\",\"data\":{\"reservation\":{\"id\":123}}}"
        );

        HostawayWebhookLambda lambda = new HostawayWebhookLambda(hostawayClient);

        APIGatewayProxyResponseEvent response = lambda.handleRequest(event, ctx);

        assertEquals(200, response.getStatusCode());
        assertEquals("ok", response.getBody());

        verify(hostawayClient, times(1))
                .getReservationsUpdatedSince(any(Instant.class));
    }
}
