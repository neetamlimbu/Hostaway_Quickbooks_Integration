package com.lodginet.integration.config;

public final class Settings {

    private Settings() {
    }

    // Hostaway
    public static String hostawayBaseUrl() {
        return getenvOrDefault("HOSTAWAY_BASE_URL", "https://api.hostaway.com/v1");
    }

    public static String hostawayClientId() {
        return getenvRequired("HOSTAWAY_CLIENT_ID");
    }

    public static String hostawayClientSecret() {
        return getenvRequired("HOSTAWAY_CLIENT_SECRET");
    }

    // QuickBooks
    public static String qbBaseUrl() {
        return getenvOrDefault("QB_BASE_URL", "https://sandbox-quickbooks.api.intuit.com/v3/company");
    }

    public static String qbRealmId() {
        return getenvRequired("QB_REALM_ID");
    }

    public static String qbTokenEndpoint() {
        return getenvOrDefault(
                "QB_TOKEN_ENDPOINT",
                "https://oauth.platform.intuit.com/oauth2/v1/tokens/bearer"
        );
    }

    public static String qbClientId() {
        return getenvRequired("QB_CLIENT_ID");
    }

    public static String qbClientSecret() {
        return getenvRequired("QB_CLIENT_SECRET");
    }

    public static String qbRedirectUri() {
        return getenvRequired("QB_REDIRECT_URI");
    }

    public static String qbRefreshToken() {
        return getenvRequired("QB_REFRESH_TOKEN");
    }

    // Helpers
    private static String getenvOrDefault(String key, String def) {
        String v = System.getenv(key);
        return (v != null && !v.isBlank()) ? v : def;
    }

    private static String getenvRequired(String key) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Missing required env var: " + key);
        }
        return v;
    }
}
