package com.example.clinic.model;

public final class InventoryItem {
    private final String id;
    private final String name;
    private final int quantity;
    private final double price;
    private final String status;

    public InventoryItem(String id, String name, int quantity, double price, String status) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }
}
