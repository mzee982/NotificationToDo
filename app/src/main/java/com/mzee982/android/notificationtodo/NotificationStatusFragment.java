package com.mzee982.android.notificationtodo;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Iterator;

public class NotificationStatusFragment extends Fragment {

    public static final String TAG = "FRAGMENT_NOTIFICATION_STATUS";
    private static final String CLASS_PREFIX = NotificationStatusFragment.class.getPackage().getName();
    private static final String ACTION_PREFIX = ".action.";
    private static final String ACTION_NOTIFICATION_STATUS_RESPONSE = CLASS_PREFIX + ACTION_PREFIX + "NOTIFICATION_STATUS_RESPONSE";

    private NotificationStatusFragmentReceiver mNotificationStatusFragmentReceiver;

    private class NotificationStatusFragmentReceiver extends BroadcastReceiver {

        public void registerForNotificationStatusResponse(Context context) {
            IntentFilter filter = new IntentFilter(ACTION_NOTIFICATION_STATUS_RESPONSE);
            LocalBroadcastManager.getInstance(context).registerReceiver(this, filter);
        }

        public void unregister(Context context) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            /*
             * Notification Status Response
             */

            if (intent.getAction().equals(ACTION_NOTIFICATION_STATUS_RESPONSE)) {

                //
                updateNotificationStatus(intent.getExtras());

            }

        }

    }

    public static Intent newNotificationStatusResponseIntent(Bundle extras) {
        Intent intent = new Intent(ACTION_NOTIFICATION_STATUS_RESPONSE);
        intent.putExtras(extras);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Local broadcast receiver
        mNotificationStatusFragmentReceiver = new NotificationStatusFragmentReceiver();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentNotificationStatus = inflater.inflate(R.layout.fragment_notification_status, container, false);

        ImageButton imageButtonNotificationStatusRefresh = (ImageButton) fragmentNotificationStatus.findViewById(R.id.imageButtonNotificationStatusRefresh);

        imageButtonNotificationStatusRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickNotificationStatusRefresh(v);
            }
        });

        return fragmentNotificationStatus;
    }

    @Override
    public void onResume() {

        // Register local broadcast receiver
        mNotificationStatusFragmentReceiver.registerForNotificationStatusResponse(getActivity());

        //
        requestNotificationStatusRefresh();

        super.onResume();
    }

    @Override
    public void onPause() {

        // Unregister local broadcast receiver
        mNotificationStatusFragmentReceiver.unregister(getActivity());

        super.onPause();
    }

    private void onClickNotificationStatusRefresh(View view) {
        requestNotificationStatusRefresh();
    }

    private void requestNotificationStatusRefresh() {

        // Reset notification status list
        updateNotificationStatus(null);

        // Notification status request
        Intent localIntent = NotificationToDoService.newNotificationStatusRequestIntent();
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(localIntent);

    }

    private void updateNotificationStatus(Bundle statusExtras) {
        LinearLayout container = (LinearLayout) getActivity().findViewById(R.id.layoutNotificationStatusContainer);

        container.removeAllViews();

        if (statusExtras != null) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            Iterator<String> keyIterator = statusExtras.keySet().iterator();

            while (keyIterator.hasNext()) {
                String key = keyIterator.next();
                String[] statusEntry = statusExtras.getStringArray(key);

                LinearLayout item = (LinearLayout) inflater.inflate(R.layout.item_notification_status, container, false);

                ((TextView) item.findViewById(R.id.textId)).setText(key);
                ((TextView) item.findViewById(R.id.textPackage)).setText(statusEntry[0]);
                ((TextView) item.findViewById(R.id.textStatus)).setText(statusEntry[1]);
                ((TextView) item.findViewById(R.id.textNotificationStatus)).setText(statusEntry[2]);

                container.addView(item);
            }
        }

    }

}
