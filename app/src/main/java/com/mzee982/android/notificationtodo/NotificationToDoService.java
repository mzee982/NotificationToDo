package com.mzee982.android.notificationtodo;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class NotificationToDoService extends NotificationListenerService {

    private static final String CLASS_PREFIX = NotificationToDoService.class.getPackage().getName();
    private static final String ACTION_PREFIX = ".action.";
    private static final String EXTRA_PREFIX = ".extra.";
    private static final String ACTION_POPUP_STATUS =  CLASS_PREFIX + ACTION_PREFIX + "POPUP_STATUS";
    private static final String ACTION_NOTIFICATION_STATUS_REQUEST = CLASS_PREFIX + ACTION_PREFIX + "NOTIFICATION_STATUS_REQUEST";
    private static final String ACTION_CONFIGURATION_CHECK_REQUEST = CLASS_PREFIX + ACTION_PREFIX + "CONFIGURATION_CHECK_REQUEST";
    private static final String ACTION_RESTART_REQUEST = CLASS_PREFIX + ACTION_PREFIX + "RESTART_REQUEST";
    private static final String EXTRA_ID = CLASS_PREFIX + EXTRA_PREFIX + "ID";
    private static final String EXTRA_POPUP_STATUS_DONE = CLASS_PREFIX + EXTRA_PREFIX + "POPUP_STATUS_DONE";
    private static final String EXTRA_POPUP_STATUS_CANCELED = CLASS_PREFIX + EXTRA_PREFIX + "POPUP_STATUS_CANCELLED";
    private static final int ONGOING_NOTIFICATION_ID = (int)System.currentTimeMillis();

    private boolean mListenerConnected = false;
    private Configuration mConfiguration;
    private HashMap<String,ToDoNotification> mRegisteredNotifications;
    private NotificationToDoServiceReceiver mBroadcastReceiver;
    private Semaphore mPopupSemaphore;
    private ConcurrentLinkedQueue<ToDoNotification> mPopupQueue;
    private ConcurrentLinkedQueue<ToDoNotification> mCanceledPopupQueue;
    private ConcurrentLinkedQueue<ToDoNotification> mRePopupQueue;

    private class NotificationToDoServiceReceiver extends BroadcastReceiver {

        private NotificationToDoServiceReceiver() {
        }

        public void register(Context context) {
            registerReceiver(this, new IntentFilter(Intent.ACTION_USER_PRESENT));
            LocalBroadcastManager.getInstance(context).registerReceiver(this, new IntentFilter(ACTION_POPUP_STATUS));
            LocalBroadcastManager.getInstance(context).registerReceiver(this, new IntentFilter(ACTION_NOTIFICATION_STATUS_REQUEST));
            LocalBroadcastManager.getInstance(context).registerReceiver(this, new IntentFilter(ACTION_CONFIGURATION_CHECK_REQUEST));
            LocalBroadcastManager.getInstance(context).registerReceiver(this, new IntentFilter(ACTION_RESTART_REQUEST));
        }

        public void unregister(Context context) {
            unregisterReceiver(this);
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            /*
             * Action User Present
             */

            if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                processPopupQueue(true);
            }

            /*
             * Popup Status Callback
             */

            else if (intent.getAction().equals(ACTION_POPUP_STATUS)) {

                // Extras
                String id = intent.getStringExtra(EXTRA_ID);
                boolean isCanceled = intent.getBooleanExtra(EXTRA_POPUP_STATUS_CANCELED, false);
                boolean isDone = intent.getBooleanExtra(EXTRA_POPUP_STATUS_DONE, false);

                //
                ToDoNotification toDoNotification = ToDoNotification.getRegistered(mRegisteredNotifications, id);

                if (toDoNotification != null) {
                    processPopupQueueCallback(toDoNotification);
                    toDoNotification.receivePopupStatus(mRegisteredNotifications, mCanceledPopupQueue, isCanceled, isDone);
                    sendNotificationStatusResponse(context);
                }

                //
                processPopupQueue(false);

            }

            /*
             * Notification Status Request
             */

            else if (intent.getAction().equals(ACTION_NOTIFICATION_STATUS_REQUEST)) {

                // Notification status response
                sendNotificationStatusResponse(context);

            }

            /*
             * Configuration Check Request
             */

            else if (intent.getAction().equals(ACTION_CONFIGURATION_CHECK_REQUEST)) {

                // Service restart request
                boolean isObsolete = mConfiguration.isObsolete(NotificationToDoService.this);
                sendServiceRestartRequest(context, isObsolete);

            }

            /*
             * Restart Request
             */

            else if (intent.getAction().equals(ACTION_RESTART_REQUEST)) {

                // Re-initialize the service
                restart(context);

            }

        }

    }

    public static Intent newPopupStatusIntent(String id, boolean canceled, boolean done) {
        Intent intent = new Intent(ACTION_POPUP_STATUS);
        intent.putExtra(NotificationToDoService.EXTRA_ID, id);
        intent.putExtra(NotificationToDoService.EXTRA_POPUP_STATUS_CANCELED, canceled);
        intent.putExtra(NotificationToDoService.EXTRA_POPUP_STATUS_DONE, done);

        return intent;
    }

    public static Intent newNotificationStatusRequestIntent() {
        Intent intent = new Intent(ACTION_NOTIFICATION_STATUS_REQUEST);
        return intent;
    }

    public static Intent newConfigurationCheckRequestIntent() {
        Intent intent = new Intent(ACTION_CONFIGURATION_CHECK_REQUEST);
        return intent;
    }

    public static Intent newRestartRequest() {
        Intent intent = new Intent(ACTION_RESTART_REQUEST);
        return intent;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initialize();
    }

    @Override
    public void onListenerConnected() {
        mListenerConnected = true;
        start();
    }

    @Override
    public void onDestroy() {
        shutdown();
        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        // Already registered as posted?
        ToDoNotification postedToDoNotification = ToDoNotification.getRegistered(mRegisteredNotifications, sbn);

        //
        if (isNotificationSelected(sbn)) {

            // Not registered yet
            if (postedToDoNotification == null) {
                postedToDoNotification = new ToDoNotification(mRegisteredNotifications, sbn);
            }

            // Register
            postedToDoNotification.register(mRegisteredNotifications, sbn);
            postedToDoNotification.onNotificationPosted(mConfiguration, mPopupQueue);
            sendNotificationStatusResponse(this);
            processPopupQueue(false);
        }

        //
        else {

            if (postedToDoNotification != null) {
                postedToDoNotification.unregister(mRegisteredNotifications);
                sendNotificationStatusResponse(this);
            }

        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

        // Already registered as posted?
        ToDoNotification removedToDoNotification = ToDoNotification.getRegistered(mRegisteredNotifications, sbn);

        //
        if (isNotificationSelected(sbn)) {

            // Not registered yet
            if (removedToDoNotification == null) {
                removedToDoNotification = new ToDoNotification(mRegisteredNotifications, sbn);
                removedToDoNotification.register(mRegisteredNotifications, sbn);
            }

            //
            removedToDoNotification.onNotificationRemoved(mConfiguration, mPopupQueue);
            removedToDoNotification.unregister(mRegisteredNotifications);
            sendNotificationStatusResponse(this);
            processPopupQueue(false);
        }

        //
        else {

            if (removedToDoNotification != null) {
                removedToDoNotification.unregister(mRegisteredNotifications);
                sendNotificationStatusResponse(this);
            }

        }

    }

    private void initialize() {

        //
        mConfiguration = new Configuration(this);
        mRegisteredNotifications = new HashMap<>();
        mPopupSemaphore = new Semaphore(1);
        mPopupQueue = new ConcurrentLinkedQueue<>();
        mCanceledPopupQueue = new ConcurrentLinkedQueue<>();
        mRePopupQueue = new ConcurrentLinkedQueue<>();

        // Post ongoing notification
        if (mConfiguration.isServiceRunInForeground()) startForeground();

    }

    private void restart(Context context) {

        //
        shutdown();
        initialize();
        if (mListenerConnected) start();

        // Service restart request
        boolean isObsolete = mConfiguration.isObsolete(NotificationToDoService.this);
        sendServiceRestartRequest(context, isObsolete);

    }

    private void start() {

        // Start broadcast receiver
        mBroadcastReceiver = new NotificationToDoServiceReceiver();
        mBroadcastReceiver.register(this);

        // Register posted notifications from notification drawer
        registerNotifications();

    }

    private void shutdown() {

        // Shut down broadcast receiver
        if (mBroadcastReceiver != null) {
            mBroadcastReceiver.unregister(this);
        }

        // Unregister notifications
        unregisterNotifications();

        // Remove ongoing notification
        stopForeground(true);

    }

    private void startForeground() {
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

    private boolean isNotificationSelected(StatusBarNotification sbn) {
        boolean packageSelected = (mConfiguration.getAppList() != null) && mConfiguration.getAppList().isPackageSelected(sbn.getPackageName());
        boolean selfPackage = sbn.getPackageName().equals(getPackageName());
        boolean selfNotificationId = sbn.getId() == ONGOING_NOTIFICATION_ID;

        return packageSelected && !(selfPackage && selfNotificationId);
    }

    private void registerNotifications() {
        StatusBarNotification[] statusBarNotifications = getActiveNotifications();

        if (statusBarNotifications != null) {
            for (StatusBarNotification sbn : statusBarNotifications) {
                if (isNotificationSelected(sbn)) {
                    ToDoNotification toDoNotification = new ToDoNotification(mRegisteredNotifications, sbn);
                    toDoNotification.register(mRegisteredNotifications, sbn);
                    toDoNotification.onNotificationPosted(mConfiguration, mPopupQueue);
                    processPopupQueue(false);
                }
            }
        }

        sendNotificationStatusResponse(this);
    }

    private void unregisterNotifications() {
        mRegisteredNotifications.clear();
        sendNotificationStatusResponse(this);
    }

    private void processPopupQueue(boolean processCanceled) {
        boolean permit = false;

        try {
            permit = mPopupSemaphore.tryAcquire();

            if (permit) {

                // Re-schedule canceled popups
                if (processCanceled) {

                    // mCanceledPopupQueue -> mRePopupQueue
                    while (!mCanceledPopupQueue.isEmpty()) {
                        mRePopupQueue.offer(mCanceledPopupQueue.poll());
                    }

                }

                // Popup re-scheduled first
                if (!mRePopupQueue.isEmpty()) {
                    ToDoNotification toDoNotification = mRePopupQueue.peek();
                    toDoNotification.popup(this);
                }

                // Popup queued
                else if (!mPopupQueue.isEmpty()) {
                    ToDoNotification toDoNotification = mPopupQueue.peek();
                    toDoNotification.popup(this);
                }

                // No more queued Popup
                else {
                    mPopupSemaphore.release();
                }

            }
        }

        catch (Exception e) {
            synchronized (mPopupSemaphore) {
                if (permit && (mPopupSemaphore.availablePermits() == 0)) mPopupSemaphore.release();
            }
        }

        finally {
            // Release semaphore later
        }

    }

    private void processPopupQueueCallback(ToDoNotification toDoNotification) {

        try {

            // Remove Popup from queues
            if (toDoNotification != null) {
                mRePopupQueue.remove(toDoNotification);
                mPopupQueue.remove(toDoNotification);
            }

        }

        finally {

            // Release semaphore
            synchronized (mPopupSemaphore) {
                if (mPopupSemaphore.availablePermits() == 0) mPopupSemaphore.release();
            }

        }

    }

    private Bundle assembleNotificationStatusResponse() {
        Bundle statusResponseBundle = new Bundle();
        Iterator<String> keySetIterator = mRegisteredNotifications.keySet().iterator();

        while (keySetIterator.hasNext()) {
            String key = keySetIterator.next();
            ToDoNotification toDoNotification = mRegisteredNotifications.get(key);

            String[] statusEntry = new String[3];
            statusEntry[0] = toDoNotification.getPackageName();
            statusEntry[1] = toDoNotification.getState();
            statusEntry[2] = toDoNotification.getNotificationState();

            statusResponseBundle.putStringArray(key, statusEntry);
        }

        return statusResponseBundle;
    }

    private void sendNotificationStatusResponse(Context context) {
        Bundle notificationStatusResponseBundle = assembleNotificationStatusResponse();
        Intent localIntent = NotificationStatusFragment.newNotificationStatusResponseIntent(notificationStatusResponseBundle);
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }

    private void sendServiceRestartRequest(Context context, boolean isObsolete) {
        Intent localIntent = ServiceStatusFragment.newServiceRestartRequestIntent(isObsolete);
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }

}
