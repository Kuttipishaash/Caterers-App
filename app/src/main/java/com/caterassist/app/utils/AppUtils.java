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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import es.dmoral.toasty.Toasty;


public class AppUtils {

    public static void setUserInfoSharedPreferences(UserDetails userDetails, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SharedPref.PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPref.USER_ID, userDetails.getUserID());
        editor.putString(SharedPref.USER_EMAIL, userDetails.getUserEmail());
        editor.putBoolean(SharedPref.USER_IS_VENDOR, userDetails.getIsVendor());
        editor.putString(SharedPref.USER_NAME, userDetails.getUserName());
        editor.putString(SharedPref.USER_STREET, userDetails.getUserStreetName());
        editor.putString(SharedPref.USER_LOC, userDetails.getUserLocationName());
        editor.putString(SharedPref.USER_DISTRICT, userDetails.getUserDistrictName());
        editor.putString(SharedPref.USER_PHONE, userDetails.getUserPhone());
        editor.putString(SharedPref.USER_IMG_URL, userDetails.getUserImageUrl());
        editor.putString(SharedPref.USER_NOTIFICATION_TOKEN, userDetails.getUserNotificationToken());
        editor.apply();
    }

    public static String getCurrentUserName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SharedPref.PREF_FILE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(SharedPref.USER_NAME, "");
    }

    public static boolean isCurrentUserVendor(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SharedPref.PREF_FILE, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(SharedPref.USER_IS_VENDOR, false);
    }

    public static UserDetails getUserInfoSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SharedPref.PREF_FILE, Context.MODE_PRIVATE);
        UserDetails userDetails = new UserDetails();
        userDetails.setUserID(sharedPreferences.getString(SharedPref.USER_ID, ""));
        userDetails.setUserEmail(sharedPreferences.getString(SharedPref.USER_EMAIL, ""));
        userDetails.setIsVendor(sharedPreferences.getBoolean(SharedPref.USER_IS_VENDOR, false));
        userDetails.setUserName(sharedPreferences.getString(SharedPref.USER_NAME, ""));
        userDetails.setUserStreetName(sharedPreferences.getString(SharedPref.USER_STREET, ""));
        userDetails.setUserLocationName(sharedPreferences.getString(SharedPref.USER_LOC, ""));
        userDetails.setUserDistrictName(sharedPreferences.getString(SharedPref.USER_DISTRICT, ""));
        userDetails.setUserImageUrl(sharedPreferences.getString(SharedPref.USER_IMG_URL, ""));
        userDetails.setUserPhone(sharedPreferences.getString(SharedPref.USER_PHONE, ""));
        userDetails.setUserNotificationToken(sharedPreferences.getString(SharedPref.USER_NOTIFICATION_TOKEN, ""));
        return userDetails;
    }

    public static void clearUserInfoSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SharedPref.PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(SharedPref.USER_ID);
        editor.remove(SharedPref.USER_EMAIL);
        editor.remove(SharedPref.USER_IS_VENDOR);
        editor.remove(SharedPref.USER_NAME);
        editor.remove(SharedPref.USER_STREET);
        editor.remove(SharedPref.USER_LOC);
        editor.remove(SharedPref.USER_DISTRICT);
        editor.remove(SharedPref.USER_PHONE);
        editor.remove(SharedPref.USER_IMG_URL);
        editor.remove(SharedPref.USER_NOTIFICATION_TOKEN);
        editor.apply();
    }

    public static void cleanUpAndLogout(Activity activity) {
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.USER_INFO_BRANCH_NAME + FirebaseUtils.USER_TOKEN_BRANCH;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
        databaseReference.setValue(null).addOnSuccessListener(aVoid -> {
            AppUtils.clearUserInfoSharedPreferences(activity);
            activity.startActivity(new Intent(activity, LoginActivity.class));
            Toasty.success(activity, "Logged out successfully!", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
            activity.finish();
        }).addOnFailureListener(e -> Toasty.error(activity, "Logout failed. Please try again!").show());

    }
}
