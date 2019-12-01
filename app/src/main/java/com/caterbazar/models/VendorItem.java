package com.caterbazar.models;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class VendorItem implements Serializable {
    private String id;
    private String name;
    private String imageUrl;
    private String category;
    private double ratePerUnit;
    private double stock;
    private String unit;

    public VendorItem() {
    }

    public VendorItem(String id, String name, String imageUrl, String category, double ratePerUnit, double stock, String unit) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.category = category;
        this.ratePerUnit = ratePerUnit;
        this.stock = stock;
        this.unit = unit;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getRatePerUnit() {
        return ratePerUnit;
    }

    public void setRatePerUnit(double ratePerUnit) {
        this.ratePerUnit = ratePerUnit;
    }

    public double getStock() {
        return stock;
    }

    public void setStock(double stock) {
        this.stock = stock;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
