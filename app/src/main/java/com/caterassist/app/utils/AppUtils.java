package com.caterassist.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.caterassist.app.models.UserDetails;
import com.caterassist.app.utils.Constants.SharedPref;


public class AppUtils {
    public static void setUserInfoSharedPreferences(UserDetails userDetails, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SharedPref.PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPref.USER_ID, userDetails.getUserID());
        editor.putString(SharedPref.USER_EMAIL, userDetails.getUserEmail());
        editor.putBoolean(SharedPref.USER_IS_VENDOR, userDetails.isVendor());
        editor.putString(SharedPref.USER_NAME, userDetails.getUserName());
        editor.putFloat(SharedPref.USER_LAT, userDetails.getUserLat());
        editor.putFloat(SharedPref.USER_LNG, userDetails.getUserLng());
        editor.putString(SharedPref.USER_IMG_URL, userDetails.getUserImageUrl());
        editor.apply();
    }

    public static UserDetails getUserInfoSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SharedPref.PREF_FILE, Context.MODE_PRIVATE);
        UserDetails userDetails = new UserDetails();
        userDetails.setUserID(sharedPreferences.getString(SharedPref.USER_ID, ""));
        userDetails.setUserEmail(sharedPreferences.getString(SharedPref.USER_EMAIL, ""));
        userDetails.setVendor(sharedPreferences.getBoolean(SharedPref.USER_IS_VENDOR, false));
        userDetails.setUserName(sharedPreferences.getString(SharedPref.USER_NAME, ""));
        userDetails.setUserLat(sharedPreferences.getFloat(SharedPref.USER_LAT, 0.0f));
        userDetails.setUserLng(sharedPreferences.getFloat(SharedPref.USER_LNG, 0.0f));
        userDetails.setUserImageUrl(sharedPreferences.getString(SharedPref.USER_IMG_URL, ""));
        return userDetails;
    }

    public static void clearUserInfoSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SharedPref.PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(SharedPref.USER_ID);
        editor.remove(SharedPref.USER_EMAIL);
        editor.remove(SharedPref.USER_IS_VENDOR);
        editor.remove(SharedPref.USER_NAME);
        editor.remove(SharedPref.USER_LAT);
        editor.remove(SharedPref.USER_LNG);
        editor.remove(SharedPref.USER_IMG_URL);
        editor.apply();
    }
}
