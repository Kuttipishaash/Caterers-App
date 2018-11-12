package com.caterassist.app.utils;

public class Constants {
    public static class SharedPref {
        public static final String PREF_FILE = "cater_assist_preferences";

        // User info keys
        public static final String USER_ID = "user_id";
        public static final String USER_NAME = "cater_assist_preferences";
        public static final String USER_EMAIL = "cater_assist_preferences";
        public static final String USER_IS_VENDOR = "cater_assist_preferences";
        public static final String USER_LAT = "cater_assist_preferences";
        public static final String USER_LNG = "cater_assist_preferences";
        public static final String USER_IMG_URL = "cater_assist_preferences";
    }

    public static class IntentExtrasKeys {
        public static final String VIEW_VENDOR_ITEMS_INTENT_VENDOR_UID = "vendor_uid";
    }

    public static class PermissionRequestCodes {
        public static final int CALL_PERMISSION_REQUEST = 1;
    }
}
