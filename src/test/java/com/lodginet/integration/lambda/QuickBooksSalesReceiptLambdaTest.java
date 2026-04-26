package com.lodginet.integration.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lodginet.integration.quickbooks.QuickBooksSalesReceiptLambda;
import com.lodginet.integration.shared.clients.HostawayClient;
import com.lodginet.integration.shared.clients.QuickBooksClient;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class QuickBooksSalesReceiptLambdaTest {


    @Test
    void testSalesReceiptCreationFlow() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        HostawayClient hostaway = mock(HostawayClient.class);
        when(hostaway.getReservation(123L))
                .thenReturn(mapper.readTree("{\"id\":123,\"totalPrice\":100,\"currency\":\"GBP\"}"));

        QuickBooksClient qb = mock(QuickBooksClient.class);
        when(qb.createSalesReceipt(anyString()))
                .thenReturn(mapper.readTree("{\"SalesReceipt\":{\"Id\":\"999\"}}"));

        Context ctx = mock(Context.class);
        when(ctx.getLogger()).thenReturn(mock(LambdaLogger.class));

        Map<String, Object> detail = Map.of("reservationId", 123);
        ScheduledEvent event = new ScheduledEvent();
        event.setDetail(detail);

        QuickBooksSalesReceiptLambda lambda =
                new QuickBooksSalesReceiptLambda(hostaway, qb);

        lambda.handleRequest(event, ctx);

        verify(hostaway).getReservation(123L);
        verify(qb).createSalesReceipt(anyString());
    }
}
