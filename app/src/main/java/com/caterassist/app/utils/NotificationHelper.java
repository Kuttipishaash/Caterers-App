package com.caterassist.app.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.caterassist.app.R;
import com.caterassist.app.activities.CatererHomeActivity;
import com.caterassist.app.activities.VendorHomeActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationHelper {
    public static final String TAG = "NotificationHelper";

    public static void displayGeneralNotification(Context context, String title, String body) {
        Intent intent;
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        } else if (AppUtils.getUserInfoSharedPreferences(context).getIsVendor()) {
            intent = new Intent(context, VendorHomeActivity.class);
        } else {
            intent = new Intent(context, CatererHomeActivity.class);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, Constants.NotificationChannelConstants.GENERAL_CHANNEL_ID)
//                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_cart))
//                .setSmallIcon(R.drawable.ic_cart)
//                .setContentTitle(title)
//                .setContentText(body)
//                .setAutoCancel(true)
//                .setSound(defaultSoundUri)
//                .setContentIntent(pendingIntent);

        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_cart)
                .setContentTitle(title)
                .setContentText(body)
                .setSound(defaultSoundUri)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(Constants.NotificationChannelConstants.GENERAL_CHANNEL_ID,
                    Constants.NotificationChannelConstants.GENERAL_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(createID(), notificationBuilder.build());
    }

    public static int createID() {
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss", Locale.US).format(now));
        return id;
    }
}
