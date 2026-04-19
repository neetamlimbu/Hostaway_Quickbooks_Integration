package com.lodginet.integration.shared.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class QuickBooksMapper {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String toSalesReceiptJson(QuickBooksObject obj) throws Exception {

        ArrayNode lineArray = mapper.createArrayNode();
        int lineNum = 1;

        for (QuickBooksLineItem item : obj.getLineItems()) {

            ObjectNode line = mapper.createObjectNode();
            line.put("Id", String.valueOf(lineNum));
            line.put("LineNum", lineNum);
            line.put("Description", item.getDescription());
            line.put("Amount", item.getAmount());
            line.put("DetailType", "SalesItemLineDetail");

            // ---- SalesItemLineDetail ----
            ObjectNode detail = mapper.createObjectNode();

            // ItemRef
            ObjectNode itemRef = mapper.createObjectNode();
            itemRef.put("value", item.getItemRefValue());
            itemRef.put("name", item.getItemRefName());
            detail.set("ItemRef", itemRef);

            detail.put("UnitPrice", item.getAmount());
            detail.put("Qty", item.getQuantity());

            // TaxCodeRef (NON = non‑taxable)
            ObjectNode taxCodeRef = mapper.createObjectNode();
            taxCodeRef.put("value", "NON");
            detail.set("TaxCodeRef", taxCodeRef);

            line.set("SalesItemLineDetail", detail);

            lineArray.add(line);
            lineNum++;
        }

        // ---- Root ----
        ObjectNode root = mapper.createObjectNode();
        root.set("Line", lineArray);

        return mapper.writeValueAsString(root);
    }
}
