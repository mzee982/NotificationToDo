package com.mzee982.android.notificationtodo;

import android.app.Fragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

public class ServiceStatusFragment extends Fragment {

    public static final String TAG = "FRAGMENT_SERVICE_STATUS";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentServiceStatus = inflater.inflate(R.layout.fragment_service_status, container, false);

        Switch switchService = (Switch) fragmentServiceStatus.findViewById(R.id.switchService);
        Button buttonTestNotification = (Button) fragmentServiceStatus.findViewById(R.id.buttonTestNotification);

        switchService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onCheckedChangedSwitchService(buttonView, isChecked);
            }
        });

        buttonTestNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickTestNotification(v);
            }
        });

        return fragmentServiceStatus;
    }

    @Override
    public void onResume() {
        if (isServiceEnabledInSettings()) {
            ((Switch) getActivity().findViewById(R.id.switchService)).setChecked(true);
            ((TextView) getActivity().findViewById(R.id.textServiceStatusSub)).setText("Enabled");
        }
        else {
            ((Switch) getActivity().findViewById(R.id.switchService)).setChecked(false);
            ((TextView) getActivity().findViewById(R.id.textServiceStatusSub)).setText("Disabled");
        }

        super.onResume();
    }

    public void onCheckedChangedSwitchService(View view, boolean isChecked) {

        // Go to Notification Access Settings - in case of change
        if (isServiceEnabledInSettings() ^ isChecked) {
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        }

    }

    public void onClickTestNotification(View view) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Notification To Do")
                .setContentText(DateFormat.getDateTimeInstance().format(new Date()))
                .setAutoCancel(true);

        Intent notificationIntent = new Intent(getActivity(), getActivity().getClass());

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getActivity());
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private boolean isServiceEnabledInSettings() {
        ContentResolver contentResolver = getActivity().getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = getActivity().getPackageName();

        return !(enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName));
    }

}
