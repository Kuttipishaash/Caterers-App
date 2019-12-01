package com.caterbazar.models;

public class UserDetails {
    private String userID;
    private String userName;
    private String userPhone;
    private String userEmail;
    private boolean isVendor;
    private String userStreetName;
    private String userLocationName;
    private String userDistrictName;
    private String userImageUrl;
    private String userNotificationToken;

    public UserDetails() {
    }

    public UserDetails(String userID, String userName, String userPhone, String userEmail,
                       boolean isVendor, String userStreetName, String userLocationName,
                       String userDistrictName, String userImageUrl, String userNotificationToken) {
        this.userID = userID;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userEmail = userEmail;
        this.isVendor = isVendor;
        this.userStreetName = userStreetName;
        this.userLocationName = userLocationName;
        this.userDistrictName = userDistrictName;
        this.userImageUrl = userImageUrl;
        this.userNotificationToken = userNotificationToken;
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

    public String getUserStreetName() {
        return userStreetName;
    }

    public void setUserStreetName(String userStreetName) {
        this.userStreetName = userStreetName;
    }

    public String getUserLocationName() {
        return userLocationName;
    }

    public void setUserLocationName(String userLocationName) {
        this.userLocationName = userLocationName;
    }

    public String getUserDistrictName() {
        return userDistrictName;
    }

    public void setUserDistrictName(String userDistrictName) {
        this.userDistrictName = userDistrictName;
    }

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public void setUserImageUrl(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }

    public String getUserNotificationToken() {
        return userNotificationToken;
    }

    public void setUserNotificationToken(String userNotificationToken) {
        this.userNotificationToken = userNotificationToken;
    }
}
