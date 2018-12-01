package com.caterassist.app.utils;

import com.caterassist.app.BuildConfig;

public class FirebaseUtils {
    //TODO:Change to userInfo
    public static final String USER_INFO_BRANCH_NAME = "userInfo/";
    public static final String FAVOURITE_VENDORS_BRANCH_NAME = "favouriteVendors/";
    public static final String VENDOR_LIST_BRANCH_NAME = "vendingList/";
    public static final String CART_BRANCH_NAME = "cart/";
    public static final String CART_ITEMS_BRANCH = "items/";
    public static final String CART_VENDOR_BRANCH = "vendorDetails/";
    public static final String ORDERS_CATERER_BRANCH = "catererOrders/";
    public static final String ORDERS_VENDOR_BRANCH = "vendorOrders/";
    public static final String CATEGORIES_BRANCH = "categories/";
    public static final String ITEMS_BRANCH = "items/";
    public static final String ORDER_ITEMS_BRANCH = "orderItems";
    public static final String ORDER_INFO_BRANCH = "orderInfo";


    public static String getDatabaseMainBranchName() {
        if (BuildConfig.DEBUG)
            return "/dev/";
        else
            return "/production/";
    }
}
