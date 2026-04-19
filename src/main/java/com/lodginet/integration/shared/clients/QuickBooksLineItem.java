package com.lodginet.integration.shared.clients;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuickBooksLineItem {

    private String description;
    private double amount;
    private int quantity;

    // QuickBooks ItemRef fields
    private String itemRefValue;
    private String itemRefName;

    public QuickBooksLineItem() {
    }

    // ===== Getters =====

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getItemRefValue() {
        return itemRefValue;
    }

    public String getItemRefName() {
        return itemRefName;
    }

    // ===== Setters =====

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setItemRefValue(String itemRefValue) {
        this.itemRefValue = itemRefValue;
    }

    public void setItemRefName(String itemRefName) {
        this.itemRefName = itemRefName;
    }

    @Override
    public String toString() {
        return "QuickBooksLineItem{" +
                "description='" + description + '\'' +
                ", amount=" + amount +
                ", quantity=" + quantity +
                ", itemRefValue='" + itemRefValue + '\'' +
                ", itemRefName='" + itemRefName + '\'' +
                '}';
    }
}
