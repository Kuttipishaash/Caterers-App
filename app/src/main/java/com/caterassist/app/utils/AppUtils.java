package com.caterassist.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.caterassist.app.activities.LoginActivity;
import com.caterassist.app.models.UserDetails;
import com.caterassist.app.utils.Constants.SharedPref;
import com.google.firebase.auth.FirebaseAuth;

import es.dmoral.toasty.Toasty;


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

    public static String getCurrentUserUID(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SharedPref.PREF_FILE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(SharedPref.USER_ID, "");
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

    public static void cleanUpAndLogout(Activity activity) {
        AppUtils.clearUserInfoSharedPreferences(activity);
        activity.startActivity(new Intent(activity, LoginActivity.class));
        Toasty.success(activity, "Logged out successfully!", Toast.LENGTH_SHORT).show();
        FirebaseAuth.getInstance().signOut();
        activity.finish();
    }
}
