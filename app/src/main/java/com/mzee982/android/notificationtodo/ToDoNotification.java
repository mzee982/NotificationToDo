package com.mzee982.android.notificationtodo;

import android.app.Notification;
import android.service.notification.StatusBarNotification;

import java.util.HashMap;

public class ToDoNotification {
    private static final String STATE_CREATED = "STATE_CREATED";

    private String mState;
    private StatusBarNotification mStatusBarNotification;
    private String mId;

    public ToDoNotification(StatusBarNotification statusBarNotification) {
        mState = STATE_CREATED;

        mStatusBarNotification = statusBarNotification;
        mId = getIdFor(mStatusBarNotification);
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

    public void register(HashMap<String,ToDoNotification> register) {
        ToDoNotification registeredNotification = register.get(mId);

        if (mState == STATE_CREATED) {

            //
            if (registeredNotification == null) {
                register.put(mId, this);
            }

            //
            else {
                switch (registeredNotification.mState) {
                    case STATE_CREATED:
                        register.remove(mId);
                        register.put(mId, this);
                        break;
                }

            }

        }

    }

    public void unregister(HashMap<String,ToDoNotification> register) {
        ToDoNotification registeredNotification = register.get(mId);

        //
        if ((registeredNotification != null) && (registeredNotification.mState == mState)) {

            switch (registeredNotification.mState) {
                case STATE_CREATED:
                    register.remove(mId);
                    break;
            }

        }

    }

}
