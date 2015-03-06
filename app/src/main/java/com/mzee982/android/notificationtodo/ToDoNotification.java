package com.mzee982.android.notificationtodo;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ToDoNotification {
    private static final String STATE_CREATED = "STATE_CREATED";
    private static final String STATE_POPUP_QUEUED = "STATE_POPUP_QUEUED";
    private static final String STATE_POPUP_CANCELED_QUEUED = "STATE_POPUP_CANCELED_QUEUED";
    private static final String STATE_POPUP = "STATE_POPUP";
    private static final String STATE_POPUP_DONE = "STATE_POPUP_DONE";
    private static final String STATE_POPUP_CANCELED = "STATE_POPUP_CANCELED";
    private static final String NOTIFICATION_STATE_NONE = "NOTIFICATION_STATE_NONE";
    private static final String NOTIFICATION_STATE_POSTED = "NOTIFICATION_STATE_POSTED";
    private static final String NOTIFICATION_STATE_REMOVED = "NOTIFICATION_STATE_REMOVED";

    private String mState;
    private String mNotificationState;
    private StatusBarNotification mStatusBarNotification;
    private String mId;

    public ToDoNotification(HashMap<String,ToDoNotification> register, StatusBarNotification statusBarNotification) {
        mState = STATE_CREATED;
        mNotificationState = NOTIFICATION_STATE_NONE;

        mStatusBarNotification = statusBarNotification;
        mId = getIdFor(register, mStatusBarNotification);
    }

    private static String getIdFor(HashMap<String,ToDoNotification> register, StatusBarNotification statusBarNotification) {
        String packageName = statusBarNotification.getPackageName();
        int sbnId = statusBarNotification.getId();
        String sbnTag = statusBarNotification.getTag();

        String baseId = packageName + " " + sbnId + (sbnTag != null ? " " + sbnTag : "");
        int selector = 0;
        String id = null;

        //
        Iterator<String> keyIterator = register.keySet().iterator();

        while (keyIterator.hasNext() && (id == null)) {
            String key = keyIterator.next();

            // Already registered
            if (key.startsWith(baseId)) {
                ToDoNotification toDoNotification = register.get(key);

                // Already registered as posted
                if (toDoNotification.mNotificationState == NOTIFICATION_STATE_POSTED) {
                    id = toDoNotification.mId;
                }

                // Already registered as removed
                else {
                    int i = key.lastIndexOf('#');
                    String foundSelectorString = key.substring(i + 1);
                    int foundSelector = Integer.parseInt(foundSelectorString);

                    if (selector <= foundSelector) {
                        selector = foundSelector + 1;
                    }
                }

            }
        }

        //
        if (id == null) {
            id = baseId + " #" + selector;
        }

        return id;
    }

    public String getId() {
        return mId;
    }

    public String getPackageName() {
        return mStatusBarNotification.getPackageName();
    }

    public String getState() {
        return mState;
    }

    public String getNotificationState() {
        return mNotificationState;
    }

    public static ToDoNotification getRegistered(HashMap<String,ToDoNotification> register, String id) {
        return register.get(id);
    }

    public static ToDoNotification getRegistered(HashMap<String,ToDoNotification> register, StatusBarNotification sbn) {
        return getRegistered(register, getIdFor(register, sbn));
    }

    public void register(HashMap<String,ToDoNotification> register, StatusBarNotification sbn) {
        ToDoNotification registeredNotification = getRegistered(register, mId);

        // Not registered yet
        if (registeredNotification == null) {
            register.put(mId, this);
        }

        // Already registered
        else {
            update(sbn);
        }

    }

    public void unregister(HashMap<String,ToDoNotification> register) {

        switch (mState) {
            case STATE_CREATED:
                register.remove(mId);
                break;
            case STATE_POPUP_QUEUED:
            case STATE_POPUP_CANCELED_QUEUED:
                break;
            case STATE_POPUP:
                break;
            case STATE_POPUP_DONE:
                register.remove(mId);
                break;
            case STATE_POPUP_CANCELED:
                break;
        }

    }

    private void update(StatusBarNotification sbn) {
        mStatusBarNotification = sbn;
    }

    private void updateFrom(ToDoNotification oldToDoNotification) {
        mState = oldToDoNotification.mState;
        mNotificationState = oldToDoNotification.mNotificationState;
    }

    private void queuePopup(ConcurrentLinkedQueue<ToDoNotification> popupQueue) {

/*
STATE_CREATED
STATE_POPUP_QUEUED
STATE_POPUP_CANCELED_QUEUED
STATE_POPUP
STATE_POPUP_DONE
STATE_POPUP_CANCELED
*/
        //
        if (mState == STATE_POPUP_CANCELED) {
            mState = STATE_POPUP_CANCELED_QUEUED;
        }
        else {
            mState = STATE_POPUP_QUEUED;
        }

        //
        popupQueue.offer(this);

    }

    public void popup(Context context) {

        //
        mState = STATE_POPUP;

        //
        Intent popupIntent = PopupActivity.newIntent(context,
                mId,
                mStatusBarNotification.getPackageName(),
                //TODO Implementation for API Level 18
                mStatusBarNotification.getNotification().extras.getString(Notification.EXTRA_TITLE),
                mStatusBarNotification.getNotification().extras.getString(Notification.EXTRA_TEXT));

        context.startActivity(popupIntent);

    }

    public void receivePopupStatus(HashMap<String,ToDoNotification> register,
                                   ConcurrentLinkedQueue<ToDoNotification> canceledPopupQueue,
                                   boolean isCanceled,
                                   boolean isDone) {

        // Done
        if (!isCanceled && isDone) {
            mState = STATE_POPUP_DONE;
        }

        // Canceled
        else if (isCanceled && !isDone) {
            mState = STATE_POPUP_CANCELED;
            queuePopup(canceledPopupQueue);
        }

        // Ignored
        else if (!isCanceled && !isDone) {
            mState = STATE_POPUP_DONE;
        }

        //
        if (mNotificationState == NOTIFICATION_STATE_POSTED) {
//            STATE_POPUP_DONE
//            STATE_POPUP_CANCELED
        }

        else if (mNotificationState == NOTIFICATION_STATE_REMOVED) {
            if (mState == STATE_POPUP_DONE) {
                unregister(register);
            }
        }

    }

    public void onNotificationPosted(Configuration configuration, ConcurrentLinkedQueue<ToDoNotification> popupQueue) {
        mNotificationState = NOTIFICATION_STATE_POSTED;

        if (configuration.getPopupTrigger().equals(Configuration.PREF_VALUE_POPUP_TRIGGER_POSTED)) {
            queuePopup(popupQueue);
        }

    }

    public void  onNotificationRemoved(Configuration configuration, ConcurrentLinkedQueue<ToDoNotification> popupQueue) {
        mNotificationState = NOTIFICATION_STATE_REMOVED;

        if (configuration.getPopupTrigger().equals(Configuration.PREF_VALUE_POPUP_TRIGGER_REMOVED)) {
            queuePopup(popupQueue);
        }

    }

}
