package com.lodginet.integration.shared.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.lodginet.integration.shared.clients.QuickBooksObject;
import com.lodginet.integration.shared.clients.QuickBooksLineItem;
import com.lodginet.integration.shared.clients.HostawayReservation;

import java.util.List;

import static com.lodginet.integration.oauth.OAuthService.mapper;

public class QuickBooksMapper {

    public static QuickBooksObject map(HostawayReservation reservation) {

        QuickBooksObject qb = new QuickBooksObject();

        // Customer name
        qb.setCustomerName(reservation.getGuestName());

        // Customer email
        qb.setCustomerEmail(reservation.getGuestEmail());

        // Amount & currency
        qb.setAmount(reservation.getTotalPrice());
        qb.setCurrency(reservation.getCurrency());

        // Due date (use checkout date as invoice due date)
        qb.setDueDate(reservation.getCheckOut());

        // Invoice number (traceability)
        qb.setInvoiceNumber("HA-" + reservation.getId());

        // Hostaway reservation ID
        qb.setHostawayReservationId(reservation.getId());

        // Line items (simple single-line invoice)
        QuickBooksLineItem item = new QuickBooksLineItem();
        item.setDescription("Reservation for listing " + reservation.getListingId());
        item.setQuantity(1);
        item.setAmount(reservation.getTotalPrice());

        qb.setLineItems(List.of(item));

        return qb;
    }

    public static String toSalesReceiptJson(JsonNode reservation) throws Exception {

        String guestName = reservation.path("guest").path("fullName").asText("Guest");
        double total = reservation.path("totalPrice").asDouble(0);
        String currency = reservation.path("currency").asText("GBP");

        var root = mapper.createObjectNode();
        var sr = root.putObject("SalesReceipt");

        sr.put("TotalAmt", total);
        sr.put("PrivateNote", "Hostaway Reservation " + reservation.path("id").asLong());
        sr.put("CurrencyRef", currency);

        var line = sr.putArray("Line").addObject();
        line.put("Amount", total);
        line.put("DetailType", "SalesItemLineDetail");

        var detail = line.putObject("SalesItemLineDetail");
        detail.put("Qty", 1);
        detail.put("UnitPrice", total);
        detail.putObject("ItemRef").put("value", "1").put("name", "Accommodation");

        return mapper.writeValueAsString(root);
    }
}