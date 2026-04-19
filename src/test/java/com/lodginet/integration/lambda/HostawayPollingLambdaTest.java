package com.lodginet.integration.lambda;

import com.lodginet.integration.polling.HostawayPollingLambda;
import com.lodginet.integration.shared.polling.PollingService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class HostawayPollingLambdaTest {

    @Test
    public void testPollingHandler() {
        // Mock PollingService
        PollingService pollingService = mock(PollingService.class);

        // Inject mock directly
        HostawayPollingLambda lambda = new HostawayPollingLambda(pollingService);

        // Execute
        String result = lambda.handleRequest(null, null);

        // Verify
        verify(pollingService).pollForMissedEvents();
        assert result.equals("Polling complete");
    }
}