package com.caterassist.app.models;

public class GenericItem {
    String itemName;
    String unit;

    public GenericItem() {
    }

    public GenericItem(String itemName, String unit) {
        this.itemName = itemName;
        this.unit = unit;
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
}
