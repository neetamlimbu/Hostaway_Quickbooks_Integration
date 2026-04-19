package com.lodginet.integration.shared.clients;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuickBooksObject {

    private String customerName;
    private String customerEmail;
    private Double amount;
    private String currency;
    private Long hostawayReservationId;

    private String invoiceNumber;
    private List<QuickBooksLineItem> lineItems;
    private String dueDate;

    public QuickBooksObject() {
    }

    // ===== Getters =====
    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public Double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Long getHostawayReservationId() {
        return hostawayReservationId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public List<QuickBooksLineItem> getLineItems() {
        return lineItems;
    }

    public String getDueDate() {
        return dueDate;
    }

    // ===== Setters =====
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setHostawayReservationId(Long hostawayReservationId) {
        this.hostawayReservationId = hostawayReservationId;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public void setLineItems(List<QuickBooksLineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    // ----------------------------------------------------------------------
    // FACTORY: HostawayReservation → QuickBooksObject (NULL-SAFE)
    // ----------------------------------------------------------------------
    public static QuickBooksObject fromHostawayEvent(HostawayReservation r) {
        QuickBooksObject obj = new QuickBooksObject();

        String guestName = r.getGuestName() != null ? r.getGuestName() : "Guest";
        String guestEmail = r.getGuestEmail() != null ? r.getGuestEmail() : "unknown@example.com";
        Double total = r.getTotalPrice() != null ? r.getTotalPrice() : 0.0;
        String currency = r.getCurrency() != null ? r.getCurrency() : "USD";

        obj.setCustomerName(guestName);
        obj.setCustomerEmail(guestEmail);
        obj.setAmount(total);
        obj.setCurrency(currency);
        obj.setHostawayReservationId(r.getId());

        obj.setInvoiceNumber("RES-" + r.getId());
        obj.setDueDate(null);

        QuickBooksLineItem line = new QuickBooksLineItem();
        line.setDescription("Reservation " + r.getId() + " for " + guestName);
        line.setAmount(total);
        line.setQuantity(1);

        // TODO: Replace with real QuickBooks ItemRef values
        line.setItemRefValue("10");
        line.setItemRefName("Accommodation");

        obj.setLineItems(List.of(line));

        return obj;
    }

    @Override
    public String toString() {
        return "QuickBooksObject{" +
                "customerName='" + customerName + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", hostawayReservationId=" + hostawayReservationId +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", lineItems=" + lineItems +
                ", dueDate='" + dueDate + '\'' +
                '}';
    }
}
