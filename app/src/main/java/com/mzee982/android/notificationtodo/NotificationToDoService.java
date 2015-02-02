package com.mzee982.android.notificationtodo;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;

import java.util.HashMap;

public class NotificationToDoService extends NotificationListenerService {

    private static final int ONGOING_NOTIFICATION_ID = (int)System.currentTimeMillis();

    private AppList mAppList;
    private HashMap<String,ToDoNotification> mRegisteredNotifications;

    @Override
    public void onCreate() {
        super.onCreate();

        //
        mAppList = new AppList(this);
        mRegisteredNotifications = new HashMap<>();

        /*
         * Post ongoing notification
         */

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setContentTitle("Notification To Do Service");
        notificationBuilder.setContentText("Running");

        Intent notificationIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setContentIntent(pendingIntent);

        startForeground(ONGOING_NOTIFICATION_ID, notificationBuilder.build());
    }

    @Override
    public void onListenerConnected() {
        registerNotifications();
    }

    @Override
    public void onDestroy() {
        unregisterNotifications();

        // Remove ongoing notification
        stopForeground(true);

        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        // Update the notification
        if (isNotificationSelected(sbn)) {
            ToDoNotification toDoNotification = new ToDoNotification(sbn);
            toDoNotification.register(mRegisteredNotifications);
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

        if (isNotificationSelected(sbn)) {
            ToDoNotification toDoNotification = new ToDoNotification(sbn);
            Notification notification = toDoNotification.getNotification();

            //
            toDoNotification.unregister(mRegisteredNotifications);

            //
            Intent popupIntent = PopupActivity.newIntent(this,
                                    sbn.getPackageName(),
                                    notification.extras.getString(Notification.EXTRA_TITLE),
                                    notification.extras.getString(Notification.EXTRA_TEXT));

            startActivity(popupIntent);
        }

    }

    private boolean isNotificationSelected(StatusBarNotification sbn) {
        boolean packageSelected = (mAppList != null) && mAppList.isPackageSelected(sbn.getPackageName());
        boolean selfPackage = sbn.getPackageName().equals(getPackageName());
        boolean selfNotificationId = sbn.getId() == ONGOING_NOTIFICATION_ID;

        return packageSelected && !(selfPackage && selfNotificationId);
    }

    private void registerNotifications() {
        StatusBarNotification[] statusBarNotifications = getActiveNotifications();

        if (statusBarNotifications != null) {
            for (StatusBarNotification sbn : statusBarNotifications) {
                if (isNotificationSelected(sbn)) {
                    ToDoNotification toDoNotification = new ToDoNotification(sbn);
                    toDoNotification.register(mRegisteredNotifications);
                }
            }
        }
    }

    private void unregisterNotifications() {
        mRegisteredNotifications.clear();
    }

}
