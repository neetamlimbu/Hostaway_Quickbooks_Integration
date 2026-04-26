package com.lodginet.integration.callback;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.lodginet.integration.config.Settings;
import com.lodginet.integration.oauth.OAuthService;
import com.lodginet.integration.shared.dynamo.RefreshTokenRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public class QuickBooksCallbackLambda implements
        RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final OAuthService oauth;
    private final RefreshTokenRepository tokenRepo;

    // AWS constructor
    public QuickBooksCallbackLambda() {
        this(
                new OAuthService(
                        Settings.qbClientId(),
                        Settings.qbClientSecret(),
                        Settings.qbRedirectUri(),
                        Settings.qbRefreshToken(),      // may be null on first run
                        Settings.qbTokenEndpoint()
                ),
                new RefreshTokenRepository(DynamoDbClient.builder().build())
        );
    }

    // Test constructor
    public QuickBooksCallbackLambda(
            OAuthService oauth,
            RefreshTokenRepository tokenRepo) {
        this.oauth = oauth;
        this.tokenRepo = tokenRepo;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent req,
                                                      Context context) {
        try {
            Map<String, String> qs = Optional.ofNullable(req.getQueryStringParameters())
                    .orElse(Map.of());

            String code = qs.get("code");
            String state = qs.get("state");

            log(context, "Received QuickBooks callback: code=" + code + ", state=" + state);

            if (code == null || code.isBlank()) {
                return html(400, "<h1>QuickBooks Connection Failed</h1><p>No authorization code.</p>");
            }

            // Exchange authorization code for tokens
            JsonNode tokenJson = oauth.exchangeAuthorizationCode(code);

            if (tokenJson == null) {
                return html(500, "<h1>QuickBooks Connection Failed</h1><p>Token response was null.</p>");
            }

            String refreshToken = tokenJson.path("refresh_token").asText(null);
            if (refreshToken == null || refreshToken.isBlank()) {
                return html(500, "<h1>QuickBooks Connection Failed</h1><p>No refresh token returned.</p>");
            }

            tokenRepo.saveRefreshToken(refreshToken);
            log(context, "Stored new QuickBooks refresh token.");

            return html(200,
                    "<h1>QuickBooks Connected</h1>" +
                            "<p>You may now close this window.</p>");

        } catch (Exception e) {
            log(context, "Error: " + e.getMessage());
            return html(500,
                    "<h1>QuickBooks Connection Failed</h1>" +
                            "<p>" + escape(e.getMessage()) + "</p>");
        }
    }

    private APIGatewayProxyResponseEvent html(int status, String body) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(status)
                .withHeaders(Map.of("Content-Type", "text/html; charset=utf-8"))
                .withBody(body);
    }

    private void log(Context ctx, String msg) {
        if (ctx != null && ctx.getLogger() != null) {
            ctx.getLogger().log(msg + "\n");
        }
    }

    private String escape(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }
}
