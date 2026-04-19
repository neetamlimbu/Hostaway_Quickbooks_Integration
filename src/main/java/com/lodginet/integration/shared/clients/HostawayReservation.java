package com.lodginet.integration.shared.clients;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HostawayReservation {

    private Long id;
    private Long listingId;
    private Integer channelId;

    private String status;

    private String guestName;
    private String guestEmail;
    private String guestPhone;

    private String checkIn;
    private String checkOut;

    private Integer numberOfGuests;

    private Double totalPrice;
    private String currency;

    private Long created;   // epoch millis
    private Long updated;   // epoch millis

    // ===== Getters =====

    public Long getId() {
        return id;
    }

    public Long getListingId() {
        return listingId;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public String getStatus() {
        return status;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public String getGuestPhone() {
        return guestPhone;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public String getCheckOut() {
        return checkOut;
    }

    public Integer getNumberOfGuests() {
        return numberOfGuests;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public Long getCreated() {
        return created;
    }

    public Long getUpdated() {
        return updated;
    }

    // ===== Setters =====

    public void setId(Long id) {
        this.id = id;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }

    public void setGuestPhone(String guestPhone) {
        this.guestPhone = guestPhone;
    }

    public void setCheckIn(String checkIn) {
        this.checkIn = checkIn;
    }

    public void setCheckOut(String checkOut) {
        this.checkOut = checkOut;
    }

    public void setNumberOfGuests(Integer numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public void setUpdated(Long updated) {
        this.updated = updated;
    }
}
