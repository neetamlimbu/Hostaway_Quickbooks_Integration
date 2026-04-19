package com.lodginet.integration.shared.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class HostawayClient {

    private final OkHttpClient http;
    private final ObjectMapper mapper;
    private final HostawayAuthService auth;
    private final String baseUrl;

    public HostawayClient(OkHttpClient http,
                          ObjectMapper mapper,
                          HostawayAuthService auth,
                          String baseUrl) {

        this.http = http;
        this.mapper = mapper;
        this.auth = auth;

        // Normalize base URL (no trailing slash)
        this.baseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
    }

    // ----------------------------------------------------------------------
    // LISTINGS
    // ----------------------------------------------------------------------

    public List<HostawayListing> getListingsUpdatedSince(Instant since) throws Exception {
        List<HostawayListing> all = new ArrayList<>();
        Integer page = 1;

        while (page != null) {
            JsonNode json = fetchListingsPage(since, page);

            JsonNode listingsNode = json.path("result").path("listings");
            if (listingsNode.isArray()) {
                for (JsonNode node : listingsNode) {
                    HostawayListing l = mapper.treeToValue(node, HostawayListing.class);

                    // Filter by updatedSince
                    Instant updated = Instant.ofEpochMilli(node.path("updated").asLong(0));
                    if (!updated.isBefore(since)) {
                        all.add(l);
                    }
                }
            }

            JsonNode nextPageNode = json.path("result").path("nextPage");
            page = nextPageNode.isMissingNode() || nextPageNode.isNull()
                    ? null
                    : nextPageNode.asInt();
        }

        return all;
    }

    private JsonNode fetchListingsPage(Instant since, int page) throws Exception {
        HttpUrl url = HttpUrl.parse(baseUrl + "/listings")
                .newBuilder()
                .addQueryParameter("updatedSince", String.valueOf(since.toEpochMilli()))
                .addQueryParameter("page", String.valueOf(page))
                .build();

        return executeWithRetry(url);
    }

    // ----------------------------------------------------------------------
    // RESERVATIONS
    // ----------------------------------------------------------------------

    public List<HostawayReservation> getReservationsUpdatedSince(Instant since) throws Exception {
        List<HostawayReservation> all = new ArrayList<>();
        Integer page = 1;

        while (page != null) {
            JsonNode json = fetchReservationsPage(since, page);

            JsonNode reservationsNode = json.path("result").path("reservations");
            if (reservationsNode.isArray()) {
                for (JsonNode node : reservationsNode) {
                    HostawayReservation r = mapper.treeToValue(node, HostawayReservation.class);
                    all.add(r);
                }
            }

            JsonNode nextPageNode = json.path("result").path("nextPage");
            page = nextPageNode.isMissingNode() || nextPageNode.isNull()
                    ? null
                    : nextPageNode.asInt();
        }

        return all;
    }

    private JsonNode fetchReservationsPage(Instant since, int page) throws Exception {
        HttpUrl url = HttpUrl.parse(baseUrl + "/reservations")
                .newBuilder()
                .addQueryParameter("updatedSince", String.valueOf(since.toEpochMilli()))
                .addQueryParameter("page", String.valueOf(page))
                .build();

        return executeWithRetry(url);
    }

    // ----------------------------------------------------------------------
    // MISSED EVENTS
    // ----------------------------------------------------------------------

    public List<HostawayReservation> fetchMissedEvents(Instant updatedSince) throws Exception {
        HttpUrl url = HttpUrl.parse(baseUrl + "/reservations")
                .newBuilder()
                .addQueryParameter("updatedSince", updatedSince.toString())
                .build();

        JsonNode root = executeWithRetry(url);
        JsonNode data = root.path("result");

        List<HostawayReservation> list = new ArrayList<>();
        if (data.isArray()) {
            for (JsonNode node : data) {
                HostawayReservation r = mapper.treeToValue(node, HostawayReservation.class);
                list.add(r);
            }
        }

        return list;
    }

    // ----------------------------------------------------------------------
    // HTTP + RETRY
    // ----------------------------------------------------------------------

    private JsonNode executeWithRetry(HttpUrl url) throws Exception {
        Request request = buildRequest(url);

        Response response = http.newCall(request).execute();

        if (response.code() == 500) {
            response.close();
            // retry once
            response = http.newCall(request).execute();
        }

        return parseJson(response);
    }

    private Request buildRequest(HttpUrl url) {
        String token = null;
        try {
            token = auth.getAccessToken();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/json")
                .build();
    }

    private JsonNode parseJson(Response response) throws IOException {
        try (response) {
            String body = response.body() != null ? response.body().string() : "";
            return mapper.readTree(body);
        }
    }
}
