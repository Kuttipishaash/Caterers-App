package com.caterbazar.models;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class OrderDetails implements Serializable {
    String orderId;
    String vendorId;
    String catererID;
    String vendorPhone;
    String catererPhone;
    String vendorName;
    String catererName;
    String vendorEmail;
    String catererEmail;
    int orderStatus;
    String orderTime;
    double orderTotalAmount;

    @Exclude
    public String getOrderId() {
        return orderId;
    }

    @Exclude
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getCatererID() {
        return catererID;
    }

    public void setCatererID(String catererID) {
        this.catererID = catererID;
    }

    public String getVendorPhone() {
        return vendorPhone;
    }

    public void setVendorPhone(String vendorPhone) {
        this.vendorPhone = vendorPhone;
    }

    public String getCatererPhone() {
        return catererPhone;
    }

    public void setCatererPhone(String catererPhone) {
        this.catererPhone = catererPhone;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getCatererName() {
        return catererName;
    }

    public void setCatererName(String catererName) {
        this.catererName = catererName;
    }

    public int getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(int orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }

    public double getOrderTotalAmount() {
        return orderTotalAmount;
    }

    public void setOrderTotalAmount(double orderTotalAmount) {
        this.orderTotalAmount = orderTotalAmount;
    }

    public String getVendorEmail() {
        return vendorEmail;
    }

    public void setVendorEmail(String vendorEmail) {
        this.vendorEmail = vendorEmail;
    }

    public String getCatererEmail() {
        return catererEmail;
    }

    public void setCatererEmail(String catererEmail) {
        this.catererEmail = catererEmail;
    }
}
