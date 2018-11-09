package com.caterassist.app.utils;

import com.caterassist.app.BuildConfig;

public class FirebaseUtils {
    //TODO:Change to userInfo
    public static final String userInfoBranchName = "/user_info/";

    public static String getDatabaseMainBranchName() {
        if (BuildConfig.DEBUG)
            return "/dev";
        else
            return "/production";
    }
}
