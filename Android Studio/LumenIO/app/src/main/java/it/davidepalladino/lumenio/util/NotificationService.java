package it.davidepalladino.lumenio.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.view.activity.MainActivity;

public class NotificationService extends Service {
    private NotificationChannel notificationChannel;

    private String channelID;
    private int notificationID = 100;

    private boolean isAlreadyShown = false;

    private PendingIntent pendingIntent = null;

    public IBinder binder = new LocalBinder();
    public class LocalBinder extends Binder {
        public NotificationService getService() {
            return NotificationService.this;
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return binder; }

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        channelID = NotificationService.class.getSimpleName();

        if (Build.VERSION.SDK_INT > 25) {
            notificationChannel = new NotificationChannel(
                    channelID,
                    getString(R.string.notification_channel_name_device),
                    NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setDescription(getString(R.string.notification_channel_description_device));

            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public void createNotification(String contentTitle, String contentText) {
        if (!isAlreadyShown) {
            isAlreadyShown = true;

            PackageManager packageManager = this.getPackageManager();
            Intent intentMainActivity = packageManager.getLaunchIntentForPackage (this.getPackageName());
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intentMainActivity,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT
            );

            Notification notification =
                    new Notification.Builder(this, channelID)
                            .setContentTitle(contentTitle)
                            .setContentText(contentText)
                            .setSmallIcon(R.drawable.ic_app_notification)
                            .setContentIntent(pendingIntent)
                            .setChannelId(channelID)
                            .build();

            startForeground(notificationID, notification);
        }
    }

    public void destroyNotification() {
        if (isAlreadyShown) {
            isAlreadyShown = false;
            stopForeground(true);
        }
    }
}
