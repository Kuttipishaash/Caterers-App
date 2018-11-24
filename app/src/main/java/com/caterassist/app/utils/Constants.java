package com.caterassist.app.utils;

public class Constants {
    public static class SharedPref {
        public static final String PREF_FILE = "caterassist_pref";

        // User info keys
        public static final String USER_ID = "pref_id";
        public static final String USER_NAME = "pref_name";
        public static final String USER_EMAIL = "pref_email";
        public static final String USER_IS_VENDOR = "pref_is_vendor";
        public static final String USER_LAT = "pref_latitude";
        public static final String USER_LNG = "perf_longitude";
        public static final String USER_IMG_URL = "pref_user_image";
        public static final String USER_PHONE = "pref_phone";
    }

    public static class IntentExtrasKeys {
        public static final String VIEW_VENDOR_ITEMS_INTENT_VENDOR_UID = "vendor_uid";
    }

    public static class PermissionRequestCodes {
        public static final int CALL_PERMISSION_REQUEST = 1;
    }
}
