package com.lodginet.integration.shared.clients;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpClientFactory {
    private static final CloseableHttpClient CLIENT = HttpClients.createDefault();

    public static CloseableHttpClient getClient() {
        return CLIENT;
    }
}
