package com.mzee982.android.notificationtodo;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;

public class NotificationDetails implements Parcelable {

    public static final Creator<NotificationDetails> CREATOR = new Creator<NotificationDetails>() {
        @Override
        public NotificationDetails createFromParcel(Parcel source) {
            return new NotificationDetails(source);
        }

        @Override
        public NotificationDetails[] newArray(int size) {
            return new NotificationDetails[0];
        }
    };

    private RemoteViews mContentView;
    private String mId;
    private String mPackageName;
    private String mTitle;
    private String mText;
    private String mWhen;

    private NotificationDetails(Parcel source) {
        mContentView = (RemoteViews) source.readParcelable(null);
        mId = source.readString();
        mPackageName = source.readString();
        mTitle = source.readString();
        mText = source.readString();
        mWhen = source.readString();
    }

    public NotificationDetails(Context context, String id, StatusBarNotification statusBarNotification) {
        Notification notification  = statusBarNotification.getNotification();

        mContentView = (notification.bigContentView != null) ? notification.bigContentView : notification.contentView;
        mId = id;
        mPackageName = statusBarNotification.getPackageName();

        extract(context, notification);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mContentView, flags);
        dest.writeString(mId);
        dest.writeString(mPackageName);
        dest.writeString(mTitle);
        dest.writeString(mText);
        dest.writeString(mWhen);
    }

    private void extract(Context context, Notification notification) {

        // API level 19 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            extract_v19(notification);
        }

        // API level 18
        else {
            extract_v18(context);
        }

    }

    @TargetApi(19)
    private void extract_v19(Notification notification) {
        Bundle extras = notification.extras;

        mTitle = extras.getString(Notification.EXTRA_TITLE);
        mText = extras.getString(Notification.EXTRA_TEXT);
        Date when = new Date(notification.when);
        mWhen = DateFormat.getTimeInstance().format(when); //TODO Check
    }

    private void extract_v18(Context context) {
        View contentView = inflateNotificationContent(context, mContentView);

        int titleId = context.getResources().getIdentifier("android:id/title", null, null);
        int textId = context.getResources().getIdentifier("android:id/text", null, null);
        int timeId = context.getResources().getIdentifier("android:id/time", null, null);
        int chronometerId = context.getResources().getIdentifier("android:id/chronometer", null, null);

        TextView titleView = (TextView) contentView.findViewById(titleId);
        TextView textView = (TextView) contentView.findViewById(textId);
        View timeView = contentView.findViewById(timeId);
        View chronometerView = contentView.findViewById(chronometerId);

        mTitle = titleView.getText().toString();
        mText = textView.getText().toString();
        if ((timeView instanceof TextView) && (timeView.getVisibility() == View.VISIBLE)) mWhen = ((TextView) timeView).getText().toString();
        else if ((chronometerView instanceof TextView) && (chronometerView.getVisibility() == View.VISIBLE)) mWhen = ((TextView) chronometerView).getText().toString();
        else mWhen = "";
    }

    private View inflateNotificationContent(Context context, RemoteViews contentRemoteView) {
        int layoutId = contentRemoteView.getLayoutId();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup contentViewGroup = (ViewGroup) inflater.inflate(layoutId, null);
        View contentView = contentRemoteView.apply(context, contentViewGroup);

        return contentView;
    }

    public View inflateSelectableNotificationContent(Context context, View.OnClickListener textSelectionListener) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinkedList<ViewGroup> traversalQueue = new LinkedList<>();

        View notificationContentView = inflateNotificationContent(context, mContentView);

        if (notificationContentView instanceof ViewGroup) {
            traversalQueue.add((ViewGroup) notificationContentView);

            while (!traversalQueue.isEmpty()) {
                ViewGroup parentViewGroup = traversalQueue.remove();

                for (int childIndex = 0; childIndex < parentViewGroup.getChildCount(); childIndex++) {
                    View childView = parentViewGroup.getChildAt(childIndex);

                    if (childView.getVisibility() == View.VISIBLE) {

                        if (childView instanceof ViewGroup) {
                            traversalQueue.add((ViewGroup) childView);
                        }

                        if (childView.hasOnClickListeners()) {
                            childView.setOnClickListener(null);
                            childView.setOnLongClickListener(null);
                            childView.setClickable(false);
                            childView.setLongClickable(false);
                        }

                        if ((childView instanceof TextView) && !(childView instanceof Button)) {
                            ViewGroup.LayoutParams childLayoutParams = childView.getLayoutParams();
                            parentViewGroup.removeView(childView);
                            LinearLayout containerNotificationContent = (LinearLayout) inflater.inflate(R.layout.container_notification_content, parentViewGroup, false);
                            LinearLayout.LayoutParams containerLayoutParams = (LinearLayout.LayoutParams) containerNotificationContent.getLayoutParams();
                            containerNotificationContent.setLayoutParams(childLayoutParams);
                            ((LinearLayout.LayoutParams) containerNotificationContent.getLayoutParams()).setMargins(
                                    containerLayoutParams.leftMargin, containerLayoutParams.topMargin,
                                    containerLayoutParams.rightMargin, containerLayoutParams.bottomMargin);
                            containerNotificationContent.addView(childView, 0);
                            parentViewGroup.addView(containerNotificationContent, childIndex);
                            containerNotificationContent.setOnClickListener(textSelectionListener);
                        }

                    }

                }

            }

        }

        return notificationContentView;
    }

    public String getId() {
        return mId;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getText() {
        return mText;
    }

}
