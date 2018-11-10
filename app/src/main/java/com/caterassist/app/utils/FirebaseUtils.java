package com.caterassist.app.utils;

import com.caterassist.app.BuildConfig;

public class FirebaseUtils {
    //TODO:Change to userInfo
    public static final String USER_INFO_BRANCH_NAME = "user_info/";
    public static final String FAVOURITE_VENDORS_BRANCH_NAME = "favouriteVendors/";

    public static String getDatabaseMainBranchName() {
        if (BuildConfig.DEBUG)
            return "/dev/";
        else
            return "/production/";
    }
}
