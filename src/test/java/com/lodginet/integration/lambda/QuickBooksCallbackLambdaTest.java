package com.lodginet.integration.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lodginet.integration.callback.QuickBooksCallbackLambda;
import com.lodginet.integration.shared.dynamo.RefreshTokenRepository;
import com.lodginet.integration.oauth.OAuthService;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class QuickBooksCallbackLambdaTest {

    @Test
    public void testCallback_exchangesCodeAndStoresRefreshToken() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        OAuthService oauth = mock(OAuthService.class);
        RefreshTokenRepository repo = mock(RefreshTokenRepository.class);
        Context ctx = mock(Context.class);

        QuickBooksCallbackLambda lambda =
                new QuickBooksCallbackLambda(oauth, repo);

        JsonNode tokenJson = mapper.readTree("""
                {
                  "access_token": "A1",
                  "refresh_token": "R1"
                }
                """);

        when(oauth.exchangeAuthorizationCode("AUTH_CODE"))
                .thenReturn(tokenJson);

        APIGatewayProxyRequestEvent req = new APIGatewayProxyRequestEvent()
                .withQueryStringParameters(Map.of(
                        "code", "AUTH_CODE",
                        "state", "xyz"
                ));

        APIGatewayProxyResponseEvent resp = lambda.handleRequest(req, ctx);

        assertEquals(200, resp.getStatusCode());
        assertTrue(resp.getBody().contains("QuickBooks Connected"));

        verify(oauth).exchangeAuthorizationCode("AUTH_CODE");
        verify(repo).saveRefreshToken("R1");
    }

    @Test
    public void testCallback_missingCode_returns400() {
        QuickBooksCallbackLambda lambda =
                new QuickBooksCallbackLambda(
                        mock(OAuthService.class),
                        mock(RefreshTokenRepository.class)
                );

        APIGatewayProxyRequestEvent req = new APIGatewayProxyRequestEvent()
                .withQueryStringParameters(Map.of("state", "xyz"));

        APIGatewayProxyResponseEvent resp = lambda.handleRequest(req, null);

        assertEquals(400, resp.getStatusCode());
        assertTrue(resp.getBody().contains("No authorization code"));
    }

    @Test
    public void testCallback_oauthThrows_returns500() throws Exception {
        OAuthService oauth = mock(OAuthService.class);
        RefreshTokenRepository repo = mock(RefreshTokenRepository.class);

        when(oauth.exchangeAuthorizationCode("AUTH_CODE"))
                .thenThrow(new RuntimeException("OAuth failed"));

        QuickBooksCallbackLambda lambda =
                new QuickBooksCallbackLambda(oauth, repo);

        APIGatewayProxyRequestEvent req = new APIGatewayProxyRequestEvent()
                .withQueryStringParameters(Map.of("code", "AUTH_CODE"));

        APIGatewayProxyResponseEvent resp = lambda.handleRequest(req, null);

        assertEquals(500, resp.getStatusCode());
        assertTrue(resp.getBody().contains("Failed"));
    }
}
