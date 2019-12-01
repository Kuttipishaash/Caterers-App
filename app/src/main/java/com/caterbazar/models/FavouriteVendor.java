package com.caterbazar.models;

public class FavouriteVendor {
    private String vendorUid;
    private String vendorName;
    private String vendorManagerName;
    private String vendorEmail;
    private String vendorPhone;
    private float vendorRating;
    private String vendorImageUrl;

    public FavouriteVendor() {
    }

    public FavouriteVendor(String vendorUid, String vendorName, String vendorManagerName, String vendorEmail, String vendorPhone, float vendorRating, String vendorImageUrl) {
        this.vendorUid = vendorUid;
        this.vendorName = vendorName;
        this.vendorManagerName = vendorManagerName;
        this.vendorEmail = vendorEmail;
        this.vendorPhone = vendorPhone;
        this.vendorRating = vendorRating;
        this.vendorImageUrl = vendorImageUrl;
    }

    public String getVendorUid() {
        return vendorUid;
    }

    public void setVendorUid(String vendorUid) {
        this.vendorUid = vendorUid;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getVendorManagerName() {
        return vendorManagerName;
    }

    public void setVendorManagerName(String vendorManagerName) {
        this.vendorManagerName = vendorManagerName;
    }

    public String getVendorEmail() {
        return vendorEmail;
    }

    public void setVendorEmail(String vendorEmail) {
        this.vendorEmail = vendorEmail;
    }

    public String getVendorPhone() {
        return vendorPhone;
    }

    public void setVendorPhone(String vendorPhone) {
        this.vendorPhone = vendorPhone;
    }

    public float getVendorRating() {
        return vendorRating;
    }

    public void setVendorRating(float vendorRating) {
        this.vendorRating = vendorRating;
    }

    public String getVendorImageUrl() {
        return vendorImageUrl;
    }

    public void setVendorImageUrl(String vendorImageUrl) {
        this.vendorImageUrl = vendorImageUrl;
    }
}
