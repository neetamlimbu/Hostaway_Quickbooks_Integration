package com.lodginet.integration.quickbooks;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lodginet.integration.config.Settings;
import com.lodginet.integration.oauth.OAuthService;
import com.lodginet.integration.shared.clients.HostawayAuthService;
import com.lodginet.integration.shared.clients.HostawayClient;
import com.lodginet.integration.shared.clients.QuickBooksClient;
import com.lodginet.integration.shared.dynamo.RefreshTokenRepository;
import com.lodginet.integration.shared.mapping.QuickBooksMapper;
import okhttp3.OkHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.Map;

public class QuickBooksSalesReceiptLambda
        implements RequestHandler<ScheduledEvent, Void> {

    private final ObjectMapper mapper = new ObjectMapper();
    private final HostawayClient hostawayClient;
    private final QuickBooksClient quickBooksClient;
    private static final String RESERVATION_ID_FIELD = "reservationId";

    /* Test constructor */
    public QuickBooksSalesReceiptLambda(
            HostawayClient hostawayClient,
            QuickBooksClient quickBooksClient
    ) {
        this.hostawayClient = hostawayClient;
        this.quickBooksClient = quickBooksClient;
    }

    /* Production constructor */
    public QuickBooksSalesReceiptLambda() {
        this(
                new HostawayClient(
                        new OkHttpClient(),
                        new ObjectMapper(),
                        new HostawayAuthService(
                                new ObjectMapper(),
                                System.getenv("HOSTAWAY_CLIENT_ID"),
                                System.getenv("HOSTAWAY_CLIENT_SECRET")
                        ),
                        System.getenv("HOSTAWAY_BASE_URL")
                ),
                new QuickBooksClient(
                        new OkHttpClient(),
                        new ObjectMapper(),
                        new OAuthService(
                                Settings.qbClientId(),
                                Settings.qbClientSecret(),
                                Settings.qbRedirectUri(),
                                Settings.qbRefreshToken(),
                                Settings.qbTokenEndpoint()
                        ),
                        new RefreshTokenRepository(DynamoDbClient.builder().build()),
                        System.getenv("QB_REALM_ID"),
                        System.getenv("QB_BASE_URL")
                )
        );
        System.out.println("DEBUG HOSTAWAY_CLIENT_ID=" + System.getenv("HOSTAWAY_CLIENT_ID"));
        System.out.println("DEBUG HOSTAWAY_CLIENT_SECRET=" + System.getenv("HOSTAWAY_CLIENT_SECRET"));
        System.out.println("DEBUG HOSTAWAY_BASE_URL=" + System.getenv("HOSTAWAY_BASE_URL"));
    }


    @Override
    public Void handleRequest(ScheduledEvent event, Context context) {

        try {
            long reservationId = extractReservationId(event);
            log(context, "EventBridge triggered for reservationId=" + reservationId);

            JsonNode reservation = hostawayClient.getReservation(reservationId);
            log(context, "Fetched reservation from Hostaway");

            String salesReceiptJson = QuickBooksMapper.toSalesReceiptJson(reservation);
            log(context, "Mapped reservation to QuickBooks SalesReceipt JSON");

            JsonNode qbResponse = quickBooksClient.createSalesReceipt(salesReceiptJson);
            log(context, "QuickBooks SalesReceipt created: " + qbResponse.toString());

        } catch (Exception e) {
            log(context, "Error: " + e.getMessage());
        }

        return null;
    }

    private long extractReservationId(ScheduledEvent event) {
        Object detailObj = event.getDetail();

        if (detailObj instanceof Map<?, ?> map) {
            Object value = map.get(RESERVATION_ID_FIELD);
            if (value instanceof Number num) {
                return num.longValue();
            }
        }

        throw new IllegalArgumentException("Invalid EventBridge detail payload");
    }


    private void log(Context ctx, String msg) {
        if (ctx != null && ctx.getLogger() != null) {
            ctx.getLogger().log(msg + "\n");
        }
    }
}
