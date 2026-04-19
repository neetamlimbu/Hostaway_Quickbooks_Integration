package com.lodginet.integration.shared.mapping;

import com.lodginet.integration.shared.clients.QuickBooksObject;
import com.lodginet.integration.shared.clients.QuickBooksLineItem;
import com.lodginet.integration.shared.clients.HostawayReservation;

import java.util.List;

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
}