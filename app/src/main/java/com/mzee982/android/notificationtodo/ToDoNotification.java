package com.mzee982.android.notificationtodo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;
import android.widget.RemoteViews;

import java.util.HashMap;

public class ToDoNotification {
    private static final String STATE_CREATED = "STATE_CREATED";
    private static final String STATE_TO_EXTEND = "STATE_TO_EXTEND";
    private static final String STATE_EXTENDED = "STATE_EXTENDED";
    private static final String STATE_TO_CLEANUP = "STATE_TO_CLEANUP";

    private String mState;
    private StatusBarNotification mStatusBarNotification;
    private String mId;
    private RemoteViews mOriginalContentView;

    public ToDoNotification(StatusBarNotification statusBarNotification) {
        mState = STATE_CREATED;

        mStatusBarNotification = statusBarNotification;
        mId = getIdFor(mStatusBarNotification);
        mOriginalContentView = null;
    }

    public static String getIdFor(StatusBarNotification statusBarNotification) {
        String packageName = statusBarNotification.getPackageName();
        int id = statusBarNotification.getId();
        String tag = statusBarNotification.getTag();

        return packageName + " " + id + (tag != null ? " " + tag : "");
    }

    public String getId() {
        return mId;
    }

    public Notification getNotification() {
        return mStatusBarNotification.getNotification();
    }

    public void register(Context context, HashMap<String,ToDoNotification> register) {
        ToDoNotification registeredNotification = register.get(mId);

        if (mState == STATE_CREATED) {

            //
            if (registeredNotification == null) {
                register.put(mId, this);
                addExtension(context);
            }

            //
            else {
                switch (registeredNotification.mState) {
                    case STATE_CREATED:
                        register.remove(mId);
                        register.put(mId, this);
                        addExtension(context);
                        break;
                    case STATE_TO_EXTEND:
                        updateInto(registeredNotification);
                        break;
                    case STATE_EXTENDED:
                        register.remove(mId);
                        register.put(mId, this);
                        addExtension(context);
                        break;
                    case STATE_TO_CLEANUP:
                        register.remove(mId);
                        break;
                }

            }

        }

    }

    public void cleanup(Context context, HashMap<String,ToDoNotification> register) {
        ToDoNotification registeredNotification = register.get(mId);

        //
        if ((registeredNotification != null) && (registeredNotification.mState == mState)) {

            switch (registeredNotification.mState) {
                case STATE_CREATED:
                    //register.remove(mId);
                    break;
                case STATE_TO_EXTEND:
                    removeExtension(context);
                    //register.remove(mId);
                    break;
                case STATE_EXTENDED:
                    removeExtension(context);
                    //register.remove(mId);
                    break;
                case STATE_TO_CLEANUP:
                    //register.remove(mId);
                    break;
            }

        }

    }

    public void unregister(Context context, HashMap<String,ToDoNotification> register) {
        ToDoNotification registeredNotification = register.get(mId);

        //
        if ((registeredNotification != null) && (registeredNotification.mState == mState)) {

            switch (registeredNotification.mState) {
                case STATE_CREATED:
                    register.remove(mId);
                    break;
                case STATE_TO_EXTEND:
                    break;
                case STATE_EXTENDED:
                    register.remove(mId);
                    break;
                case STATE_TO_CLEANUP:
                    register.remove(mId);
                    break;
            }

        }

    }

    private void addExtension(Context context) {

        if (mState == STATE_CREATED) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification extendedNotification = mStatusBarNotification.getNotification().clone();

            Intent popupIntent = new Intent(context, PopupActivity.class);
            popupIntent.putExtra("PACKAGE_NAME", mStatusBarNotification.getPackageName());
            popupIntent.putExtra("TITLE", extendedNotification.extras.getString(Notification.EXTRA_TITLE));
            popupIntent.putExtra("TEXT", extendedNotification.extras.getString(Notification.EXTRA_TEXT));

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(PopupActivity.class);
            stackBuilder.addNextIntent(popupIntent);
            PendingIntent popupPendingIntent = stackBuilder.getPendingIntent(mStatusBarNotification.getId(), PendingIntent.FLAG_UPDATE_CURRENT);

            RemoteViews extensionViews = new RemoteViews(context.getPackageName(), R.layout.notification_extension);
            extensionViews.setOnClickPendingIntent(R.id.buttonNotificationExtension, popupPendingIntent);
            int iconGroupId = context.getResources().getIdentifier("icon_group", "id", "android");

            mOriginalContentView = extendedNotification.contentView.clone();
            extendedNotification.contentView.addView(iconGroupId, extensionViews);

            mState = STATE_TO_EXTEND;

            notificationManager.notify(mStatusBarNotification.getTag(), mStatusBarNotification.getId(), extendedNotification);
        }

    }

    private void removeExtension(Context context) {

        if ((mState == STATE_EXTENDED) || (mState == STATE_TO_EXTEND)) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification cleanedupNotification = mStatusBarNotification.getNotification().clone();

            RemoteViews emptyNotificationView = new RemoteViews(context.getPackageName(), R.layout.notification_empty);
            cleanedupNotification.contentView = emptyNotificationView;

            mState = STATE_TO_CLEANUP;

            notificationManager.notify(mStatusBarNotification.getTag(), mStatusBarNotification.getId(), cleanedupNotification);

            cleanedupNotification.contentView = mOriginalContentView;

            notificationManager.notify(mStatusBarNotification.getTag(), mStatusBarNotification.getId(), cleanedupNotification);
        }

    }

    private void updateInto(ToDoNotification toDoNotification) {

        switch (toDoNotification.mState) {
            case STATE_TO_EXTEND:
                toDoNotification.mStatusBarNotification = this.mStatusBarNotification;
                toDoNotification.mState = STATE_EXTENDED;
                break;
        }

    }

}
