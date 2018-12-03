package com.caterassist.app.models;

import java.util.ArrayList;

public class Order {
    OrderDetails orderInfo;
    ArrayList<CartItem> orderItems;

    public OrderDetails getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(OrderDetails orderInfo) {
        this.orderInfo = orderInfo;
    }

    public ArrayList<CartItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(ArrayList<CartItem> orderItems) {
        this.orderItems = orderItems;
    }
}
