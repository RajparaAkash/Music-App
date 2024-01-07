package com.example.musicdemo.Utils.Helper;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.musicdemo.Activity.MainActivity;
import com.example.musicdemo.R;

public class ConversionNotification {

    public static final String CHANNEL_1_ID = "channel1_progress";
    private Context context;
    private Intent mainActivityIntent;
    PendingIntent mainActivityPendingIntent;
    private NotificationManagerCompat notificationManagerCompat;
    private NotificationCompat.Builder notificationProgress;

    @SuppressLint("WrongConstant")
    public ConversionNotification(Context context) {
        this.context = context;
        this.notificationManagerCompat = NotificationManagerCompat.from(context);
        createNototificationChannels();
        Intent intent = new Intent(context, MainActivity.class);
        this.mainActivityIntent = intent;
        intent.putExtra("show_music", true);
        this.mainActivityPendingIntent = PendingIntent.getActivity(context, 0, this.mainActivityIntent, Build.VERSION.SDK_INT >= 31 ? 167772160 : 134217728);
    }

    private void createNototificationChannels() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_1_ID, "Conversion progress", NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setDescription("Conversion progress description");
            ((NotificationManager) this.context.getSystemService(NotificationManager.class)).createNotificationChannel(notificationChannel);
        }
    }

    @SuppressLint("MissingPermission")
    public void startConvertion(String str, int i) {
        NotificationCompat.Builder contentIntent = new NotificationCompat.Builder(this.context, CHANNEL_1_ID).setSmallIcon(R.drawable.extract_conversion_progress_24dp).setContentTitle(str).setContentText(getNotificationInfoText(i)).setPriority(-1).setProgress(100, 0, false).setContentIntent(this.mainActivityPendingIntent);
        this.notificationProgress = contentIntent;
        this.notificationManagerCompat.notify(1, contentIntent.build());
    }

    @SuppressLint("MissingPermission")
    public void setProgress(int i) {
        this.notificationProgress.setProgress(100, i, false);
        this.notificationManagerCompat.notify(1, this.notificationProgress.build());
    }

    public void conversionError(String str) {
        conversionError(str, null);
    }

    @SuppressLint("MissingPermission")
    public void conversionError(String str, String str2) {
        if (this.notificationProgress == null) {
            this.notificationProgress = new NotificationCompat.Builder(this.context, CHANNEL_1_ID);
        }
        this.notificationProgress.setProgress(0, 0, false).setContentTitle(str).setSmallIcon(R.drawable.extract_error_outline_black_24dp);
        if (str2 == null) {
            this.notificationProgress.setContentText("conversion error!");
        } else {
            this.notificationProgress.setContentText(str2);
        }
        this.notificationManagerCompat.notify(1, this.notificationProgress.build());
    }

    @SuppressLint("MissingPermission")
    public void conversionSucess() {
        this.notificationProgress.setContentText("conversion sucess!").setProgress(0, 0, false).setSmallIcon(R.drawable.extract_check_black_24dp);
        this.notificationManagerCompat.notify(1, this.notificationProgress.build());
    }

    private String getNotificationInfoText(int i) {
        return i > 1 ? "Waiting for conversion " + (i - 1) + " items" : "";
    }

    public void deleteAllNotifications() {
        this.notificationManagerCompat.cancelAll();
    }
}
