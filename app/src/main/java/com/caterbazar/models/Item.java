package com.caterbazar.models;

public class Item {
    String itemName;
    String unit;
    String itemImageURL;

    public Item() {
    }

    public Item(String itemName, String unit, String itemImageURL) {
        this.itemName = itemName;
        this.unit = unit;
        this.itemImageURL = itemImageURL;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getItemImageURL() {
        return itemImageURL;
    }

    public void setItemImageURL(String itemImageURL) {
        this.itemImageURL = itemImageURL;
    }
}
