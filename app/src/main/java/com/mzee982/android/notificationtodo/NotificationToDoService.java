package com.mzee982.android.notificationtodo;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;

import java.util.HashMap;
import java.util.Iterator;

public class NotificationToDoService extends NotificationListenerService {

    private static final String TAG = NotificationToDoService.class.getSimpleName();
    private static final int ONGOING_NOTIFICATION_ID = (int)System.currentTimeMillis();

    private AppList mAppList;
    private HashMap<String,ToDoNotification> mRegisteredNotifications;

    @Override
    public void onCreate() {
        super.onCreate();

        //
        mAppList = new AppList(this);
        mRegisteredNotifications = new HashMap<String,ToDoNotification>();

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
//        stopForeground(true);

        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        // Update the notification
        if (isNotificationSelected(sbn)) {
            ToDoNotification toDoNotification = new ToDoNotification(sbn);
            toDoNotification.register(this, mRegisteredNotifications);
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

        if (isNotificationSelected(sbn)) {
            ToDoNotification toDoNotification = new ToDoNotification(sbn);
            Notification notification = toDoNotification.getNotification();

            //
            toDoNotification.unregister(this, mRegisteredNotifications);

            //
            Intent popupIntent = new Intent(this, PopupActivity.class);
            popupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            popupIntent.putExtra("PACKAGE_NAME", sbn.getPackageName());
            popupIntent.putExtra("TITLE", notification.extras.getString(Notification.EXTRA_TITLE));
            popupIntent.putExtra("TEXT", notification.extras.getString(Notification.EXTRA_TEXT));

            startActivity(popupIntent);
        }

    }

    private boolean isNotificationSelected(StatusBarNotification sbn) {
        boolean packageSelected = (mAppList != null) && mAppList.isPackageSelected(sbn.getPackageName());
        boolean selfPackage = sbn.getPackageName().equals(getPackageName());
        boolean selfNotificationId = sbn.getId() == ONGOING_NOTIFICATION_ID;
        boolean emptyNotification = sbn.getNotification().contentView.getLayoutId() == R.layout.notification_empty;

        return packageSelected && !(selfPackage && selfNotificationId) && !emptyNotification;
    }

    private void registerNotifications() {
        StatusBarNotification[] statusBarNotifications = getActiveNotifications();

        if (statusBarNotifications != null) {
            for (StatusBarNotification sbn : statusBarNotifications) {
                if (isNotificationSelected(sbn)) {
                    ToDoNotification toDoNotification = new ToDoNotification(sbn);
                    toDoNotification.register(this, mRegisteredNotifications);
                }
            }
        }
    }

    private void unregisterNotifications() {
        Iterator<String> keyIterator = mRegisteredNotifications.keySet().iterator();

        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            ToDoNotification toDoNotification = mRegisteredNotifications.get(key);

            toDoNotification.cleanup(this, mRegisteredNotifications);
        }

        mRegisteredNotifications.clear();
    }

}
