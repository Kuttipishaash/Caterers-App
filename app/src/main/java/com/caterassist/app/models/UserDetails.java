package com.caterassist.app.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserDetails {
    private String userID;
    private String userName;
    private String userPhone;
    private String userEmail;
    private boolean isVendor;
    private float userLat;
    private float userLng;
    private String userImageUrl;

    public UserDetails() {
    }

    public UserDetails(String userID, String userName, String userPhone, String userEmail,
                       boolean isVendor, float userLat, float userLng, String userImageUrl) {
        this.userID = userID;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userEmail = userEmail;
        this.isVendor = isVendor;
        this.userLat = userLat;
        this.userLng = userLng;
        this.userImageUrl = userImageUrl;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public boolean getIsVendor() {
        return isVendor;
    }

    public void setIsVendor(boolean vendor) {
        isVendor = vendor;
    }

    public float getUserLat() {
        return userLat;
    }

    public void setUserLat(float userLat) {
        this.userLat = userLat;
    }

    public float getUserLng() {
        return userLng;
    }

    public void setUserLng(float userLng) {
        this.userLng = userLng;
    }

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public void setUserImageUrl(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }
}
