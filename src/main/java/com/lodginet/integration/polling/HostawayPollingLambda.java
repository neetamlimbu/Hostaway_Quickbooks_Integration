package com.lodginet.integration.polling;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lodginet.integration.config.Settings;
import com.lodginet.integration.shared.clients.HostawayAuthService;
import com.lodginet.integration.shared.clients.HostawayClient;
import com.lodginet.integration.shared.clients.QuickBooksClient;
import com.lodginet.integration.shared.clients.SalesReceiptService;
import com.lodginet.integration.shared.dynamo.DynamoDbSyncStateRepository;
import com.lodginet.integration.shared.dynamo.RefreshTokenRepository;
import com.lodginet.integration.oauth.OAuthService;
import com.lodginet.integration.shared.polling.PollingService;
import okhttp3.OkHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class HostawayPollingLambda implements RequestHandler<Object, String> {

    private final PollingService pollingService;

    // ----------------------------------------------------------------------
    // DEFAULT AWS CONSTRUCTOR
    // ----------------------------------------------------------------------
    public HostawayPollingLambda() {
        this(
                new OkHttpClient(),
                new ObjectMapper(),
                DynamoDbClient.builder().build(),
                Settings.hostawayClientId(),
                Settings.hostawayClientSecret(),
                Settings.hostawayBaseUrl(),
                Settings.qbClientId(),
                Settings.qbClientSecret(),
                Settings.qbRedirectUri(),
                Settings.qbRealmId(),
                Settings.qbBaseUrl(),
                Settings.qbRefreshToken(),
                Settings.qbTokenEndpoint()
        );
    }

    // ----------------------------------------------------------------------
    // MAIN CONSTRUCTOR (AWS + integration tests)
    // ----------------------------------------------------------------------
    public HostawayPollingLambda(
            OkHttpClient http,
            ObjectMapper mapper,
            DynamoDbClient dynamo,
            String hostawayClientId,
            String hostawayClientSecret,
            String hostawayBaseUrl,
            String qbClientId,
            String qbClientSecret,
            String qbRedirectUri,
            String qbRealmId,
            String qbBaseUrl,
            String qbRefreshToken,
            String qbTokenEndpoint
    ) {

        // ---------------------------
        // Hostaway Public API
        // ---------------------------
        HostawayAuthService hostawayAuth = new HostawayAuthService(
                mapper,
                hostawayClientId,
                hostawayClientSecret
        );

        HostawayClient hostaway = new HostawayClient(
                http,
                mapper,
                hostawayAuth,
                hostawayBaseUrl
        );

        // ---------------------------
        // QuickBooks OAuth (FIXED)
        // ---------------------------
        OAuthService oauth = new OAuthService(
                qbClientId,
                qbClientSecret,
                qbRedirectUri,
                qbRefreshToken,
                qbTokenEndpoint
        );

        RefreshTokenRepository tokenRepo = new RefreshTokenRepository(dynamo);

        QuickBooksClient quickbooks = new QuickBooksClient(
                http,
                mapper,
                oauth,
                tokenRepo,
                qbRealmId,
                qbBaseUrl
        );

        SalesReceiptService sales = new SalesReceiptService(quickbooks);

        // ---------------------------
        // Sync State Repository
        // ---------------------------
        DynamoDbSyncStateRepository syncRepo =
                new DynamoDbSyncStateRepository(dynamo, "SyncStateTable");

        // ---------------------------
        // Polling Service
        // ---------------------------
        this.pollingService = new PollingService(hostaway, sales, syncRepo);
    }

    // ----------------------------------------------------------------------
    // TEST CONSTRUCTOR
    // ----------------------------------------------------------------------
    public HostawayPollingLambda(PollingService pollingService) {
        this.pollingService = pollingService;
    }

    // ----------------------------------------------------------------------
    // LAMBDA HANDLER
    // ----------------------------------------------------------------------
    @Override
    public String handleRequest(Object input, Context context) {
        pollingService.pollForMissedEvents();
        return "Polling complete";
    }
}
