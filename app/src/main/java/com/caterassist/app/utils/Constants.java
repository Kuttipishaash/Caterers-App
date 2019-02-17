package com.caterassist.app.utils;

public class Constants {
    public static class SharedPref {
        public static final String PREF_FILE = "caterassist_pref";

        // User info keys
        public static final String USER_ID = "pref_id";
        public static final String USER_NAME = "pref_name";
        public static final String USER_EMAIL = "pref_email";
        public static final String USER_IS_VENDOR = "pref_is_vendor";
        public static final String USER_STREET = "pref_street";
        public static final String USER_LOC = "perf_location";
        public static final String USER_DISTRICT = "perf_district";
        public static final String USER_IMG_URL = "pref_user_image";
        public static final String USER_PHONE = "pref_phone";
        public static final String USER_NOTIFICATION_TOKEN = "pref_notif_token";
    }

    public static class IntentExtrasKeys {
        public static final String VIEW_VENDOR_ITEMS_INTENT_VENDOR_UID = "vendor_uid";
        public static final String USER_ID = "user_id";
        public static final String ORDER_DETAILS_BRANCH = "order_details_branch";
        public static final String ORDER_ID = "order_id";
        public static final String ORDER_INFO = "order_info";
    }

    public static class PermissionRequestCodes {
        public static final int CALL_PERMISSION_REQUEST = 1;
    }

    public static class NotificationChannelConstants {
        public static final String GENERAL_CHANNEL_ID = "general";
        public static final String GENERAL_CHANNEL_NAME = "General";
    }

    public static class UtilConstants {
        public static final int LOADING_TIMEOUT = 10000; // 3 Seconds
    }
}
