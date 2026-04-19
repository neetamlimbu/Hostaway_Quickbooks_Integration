package com.lodginet.integration.shared.json;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class JsonValue {

    private JsonValue() {
        // Utility class
    }

    // ------------------------------------------------------------
    // TEXT
    // ------------------------------------------------------------
    public static String text(JsonNode node, String field) {
        JsonNode n = node.path(field);
        return n.isTextual() ? n.asText() : null;
    }

    // ------------------------------------------------------------
    // NUMBER (Long)
    // ------------------------------------------------------------
    public static Long number(JsonNode node, String field) {
        JsonNode n = node.path(field);
        return n.isNumber() ? n.asLong() : null;
    }

    // ------------------------------------------------------------
    // DECIMAL (Double)
    // ------------------------------------------------------------
    public static Double decimal(JsonNode node, String field) {
        JsonNode n = node.path(field);
        return n.isNumber() ? n.asDouble() : null;
    }

    // ------------------------------------------------------------
    // BOOLEAN
    // ------------------------------------------------------------
    public static Boolean bool(JsonNode node, String field) {
        JsonNode n = node.path(field);
        return n.isBoolean() ? n.asBoolean() : null;
    }

    // ------------------------------------------------------------
    // INSTANT (epoch millis)
    // ------------------------------------------------------------
    public static Instant instant(JsonNode node, String field) {
        JsonNode n = node.path(field);
        return n.isNumber() ? Instant.ofEpochMilli(n.asLong()) : null;
    }

    // ------------------------------------------------------------
    // ARRAY
    // ------------------------------------------------------------
    public static List<JsonNode> array(JsonNode node, String field) {
        JsonNode n = node.path(field);
        List<JsonNode> list = new ArrayList<>();

        if (n.isArray()) {
            n.forEach(list::add);
        }

        return list;
    }
}