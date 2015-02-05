package com.mzee982.android.notificationtodo;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import java.util.HashMap;

public class NotificationToDoService extends NotificationListenerService {

    public static final String PREFIX = NotificationToDoService.class.getPackage().getName();
    public static final String ACTION_POPUP_STATUS =  PREFIX + ".action." + "POPUP_STATUS";
    public static final String EXTRA_ID = PREFIX + ".extra." + "ID";
    public static final String EXTRA_POPUP_STATUS_DONE = PREFIX + ".extra." + "POPUP_STATUS_DONE";
    public static final String EXTRA_POPUP_STATUS_CANCELED = PREFIX + ".extra." + "POPUP_STATUS_CANCELLED";
    private static final int ONGOING_NOTIFICATION_ID = (int)System.currentTimeMillis();

    private AppList mAppList;
    private HashMap<String,ToDoNotification> mRegisteredNotifications;
    private NotificationToDoServiceReceiver mBroadcastReceiver;

    private class NotificationToDoServiceReceiver extends BroadcastReceiver {

        private NotificationToDoServiceReceiver() {
        }

        public void registerForPopupStatus(Context context) {
            IntentFilter filter = new IntentFilter(ACTION_POPUP_STATUS);
            LocalBroadcastManager.getInstance(context).registerReceiver(this, filter);
        }

        public void registerForUserPresent(Context context) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
            registerReceiver(this, filter);
        }

        public void unregister(Context context) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
            unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(ACTION_POPUP_STATUS)) {

                //
                String id = intent.getStringExtra(EXTRA_ID);
                boolean isCanceled = intent.getBooleanExtra(EXTRA_POPUP_STATUS_CANCELED, false);
                boolean isDone = intent.getBooleanExtra(EXTRA_POPUP_STATUS_DONE, false);

                //
                ToDoNotification toDoNotification = ToDoNotification.getRegistered(mRegisteredNotifications, id);

                //
                if (toDoNotification != null) {
                    toDoNotification.receivePopupStatus(mRegisteredNotifications, isCanceled, isDone);
                }

            }

            else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {

                //
                ToDoNotification.popupCanceled(NotificationToDoService.this, mRegisteredNotifications);

            }

        }

    }

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

        //
        mBroadcastReceiver = new NotificationToDoServiceReceiver();
        mBroadcastReceiver.registerForPopupStatus(this);
        mBroadcastReceiver.registerForUserPresent(this);

        //
        registerNotifications();

    }

    @Override
    public void onDestroy() {

        //
        if (mBroadcastReceiver != null) {
            mBroadcastReceiver.unregister(this);
        }

        //
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

            //
            toDoNotification.register(mRegisteredNotifications);
            toDoNotification.onNotificationPosted(this);

        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

        if (isNotificationSelected(sbn)) {
            String id = ToDoNotification.getIdFor(sbn);
            ToDoNotification toDoNotification = ToDoNotification.getRegistered(mRegisteredNotifications, id);

            if (toDoNotification == null) {
                toDoNotification = new ToDoNotification(sbn);
                toDoNotification.register(mRegisteredNotifications);
            }

            //
            toDoNotification.onNotificationRemoved(this);
            toDoNotification.unregister(mRegisteredNotifications);

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
                    //TODO Check
                    toDoNotification.onNotificationPosted(this);
                }
            }
        }
    }

    private void unregisterNotifications() {
        mRegisteredNotifications.clear();
    }

}
