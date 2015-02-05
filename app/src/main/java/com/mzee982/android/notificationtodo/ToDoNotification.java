package com.mzee982.android.notificationtodo;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;

import java.util.HashMap;
import java.util.Iterator;

public class ToDoNotification {
    private static final String STATE_CREATED = "STATE_CREATED";
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

    public ToDoNotification(StatusBarNotification statusBarNotification) {
        mState = STATE_CREATED;
        mNotificationState = NOTIFICATION_STATE_NONE;

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

    public static ToDoNotification getRegistered(HashMap<String,ToDoNotification> register, String id) {
        return register.get(id);
    }

    public void register(HashMap<String,ToDoNotification> register) {
        ToDoNotification registeredNotification = register.get(mId);

        if (mState == STATE_CREATED) {

            // Not registered yet
            if (registeredNotification == null) {
                register.put(mId, this);
            }

            // Already registered
            else {
                switch (registeredNotification.mState) {
                    case STATE_CREATED:
                        register.remove(mId);
                        register.put(mId, this);
                        break;
                    case STATE_POPUP:
//                        updateFrom(registeredNotification);
                        register.remove(mId);
                        register.put(mId, this);
                        break;
                    case STATE_POPUP_DONE:
                        register.remove(mId);
                        register.put(mId, this);
                        break;
                    case STATE_POPUP_CANCELED:
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

            switch (mState) {
                case STATE_CREATED:
                    register.remove(mId);
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

    }

    private void updateFrom(ToDoNotification oldToDoNotification) {
        mState = oldToDoNotification.mState;
        mNotificationState = oldToDoNotification.mNotificationState;
    }

    private void popup(Context context) {

        //
        mState = STATE_POPUP;

        //
        Intent popupIntent = PopupActivity.newIntent(context,
                mId,
                mStatusBarNotification.getPackageName(),
                mStatusBarNotification.getNotification().extras.getString(Notification.EXTRA_TITLE),
                mStatusBarNotification.getNotification().extras.getString(Notification.EXTRA_TEXT));

        context.startActivity(popupIntent);

    }

    public static void popupCanceled(Context context, HashMap<String,ToDoNotification> register) {
        Iterator<String> idIterator = register.keySet().iterator();

        while (idIterator.hasNext()) {
            ToDoNotification toDoNotification = register.get(idIterator.next());

            if (toDoNotification.mState == STATE_POPUP_CANCELED) {
                toDoNotification.popup(context);
            }
        }

    }

    public void receivePopupStatus(HashMap<String,ToDoNotification> register, boolean isCanceled, boolean isDone) {

        // Done
        if (!isCanceled && isDone) {
            mState = STATE_POPUP_DONE;
        }

        // Canceled
        else if (isCanceled && !isDone) {
            mState = STATE_POPUP_CANCELED;
        }

        // Ignored
        else if (!isCanceled && !isDone) {
            mState = STATE_POPUP_DONE;
        }

/*
x 0 0 ignored -> done
x 0 1 done
x 1 0 canceled
  1 1 invalid
*/

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

    public void onNotificationPosted(Context context) {
        mNotificationState = NOTIFICATION_STATE_POSTED;

        //TODO Need to popup?
        if (false) {
            popup(context);
        }

    }

    public void  onNotificationRemoved(Context context) {
        mNotificationState = NOTIFICATION_STATE_REMOVED;

        //TODO Need to popup?
        if (true) {
            popup(context);
        }

    }

}
