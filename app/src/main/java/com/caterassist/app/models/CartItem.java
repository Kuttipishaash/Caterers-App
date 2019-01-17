package com.caterassist.app.models;

import com.google.firebase.database.Exclude;

public class CartItem implements Cloneable {
    @Exclude
    private String id;

    private String name;
    private double rate;
    private double quantity;
    private String unit;
    private String imageURL;
    private double totalAmount;

    public CartItem() {
    }

    public CartItem(String name, double rate, double quantity, String unit, String imageURL, double totalAmount) {
        this.name = name;
        this.rate = rate;
        this.quantity = quantity;
        this.unit = unit;
        this.imageURL = imageURL;
        this.totalAmount = totalAmount;
    }

    public CartItem(String id, String name, double rate, double quantity, String unit, String imageURL, double totalAmount) {
        this.id = id;
        this.name = name;
        this.rate = rate;
        this.quantity = quantity;
        this.unit = unit;
        this.imageURL = imageURL;
        this.totalAmount = totalAmount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Override
    public Object clone() {
        CartItem clone;
        try {
            clone = (CartItem) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return clone;
    }
}
