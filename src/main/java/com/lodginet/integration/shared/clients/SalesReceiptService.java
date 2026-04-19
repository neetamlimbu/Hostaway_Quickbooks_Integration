package com.lodginet.integration.shared.clients;

import com.fasterxml.jackson.databind.JsonNode;

public class SalesReceiptService {

    private final QuickBooksClient qb;

    public SalesReceiptService(QuickBooksClient qb) {
        this.qb = qb;
    }

    public JsonNode createReceipt(QuickBooksObject obj) throws Exception {
        String json = QuickBooksMapper.toSalesReceiptJson(obj);
        JsonNode response = qb.createSalesReceipt(json);

        if (response.has("Fault")) {
            throw new IllegalStateException("QuickBooks error: " + response.get("Fault"));
        }

        return response;
    }

}
