package com.mzee982.android.notificationtodo;

import android.app.Fragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

public class ServiceStatusFragment extends Fragment {
    public static final String TAG = "FRAGMENT_SERVICE_STATUS";
    private static final String CLASS_PREFIX = ServiceStatusFragment.class.getPackage().getName();
    private static final String ACTION_PREFIX = ".action.";
    private static final String EXTRA_PREFIX = ".extra.";
    private static final String ACTION_SERVICE_RESTART_REQUEST = CLASS_PREFIX + ACTION_PREFIX + "SERVICE_RESTART_REQUEST";
    private static final String EXTRA_IS_OBSOLETE = CLASS_PREFIX + EXTRA_PREFIX + "IS_OBSOLETE";

    private ServiceStatusFragmentReceiver mServiceStatusFragmentReceiver;

    private class ServiceStatusFragmentReceiver extends BroadcastReceiver {

        public void register(Context context) {
            LocalBroadcastManager.getInstance(context).registerReceiver(this, new IntentFilter(ACTION_SERVICE_RESTART_REQUEST));
        }

        public void unregister(Context context) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            /*
             * Service Restart Request
             */

            if (intent.getAction().equals(ACTION_SERVICE_RESTART_REQUEST)) {
                boolean isObsolete = intent.getBooleanExtra(EXTRA_IS_OBSOLETE, false);
                updateServiceRestartWarning(isObsolete);
            }

        }

    }

    public static Intent newServiceRestartRequestIntent(boolean isObsolete) {
        Intent intent = new Intent(ACTION_SERVICE_RESTART_REQUEST);
        intent.putExtra(EXTRA_IS_OBSOLETE, isObsolete);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Local broadcast receiver
        mServiceStatusFragmentReceiver = new ServiceStatusFragmentReceiver();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentServiceStatus = inflater.inflate(R.layout.fragment_service_status, container, false);

        Switch switchService = (Switch) fragmentServiceStatus.findViewById(R.id.switchService);
        Button buttonServiceRestart = (Button) fragmentServiceStatus.findViewById(R.id.buttonServiceRestart);
        Button buttonTestNotification = (Button) fragmentServiceStatus.findViewById(R.id.buttonTestNotification);

        switchService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onCheckedChangedSwitchService(buttonView, isChecked);
            }
        });

        buttonServiceRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickServiceRestart(v);
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

        // Register local broadcast receiver
        mServiceStatusFragmentReceiver.register(getActivity());

        //
        updateServiceStatusSub(isServiceEnabledInSettings());

        //
        requestConfigurationCheck();

        super.onResume();
    }

    @Override
    public void onPause() {

        // Unregister local broadcast receiver
        mServiceStatusFragmentReceiver.unregister(getActivity());

        super.onPause();
    }

    public void onCheckedChangedSwitchService(View view, boolean isChecked) {

        // Go to Notification Access Settings - in case of change
        if (isServiceEnabledInSettings() ^ isChecked) {
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        }

    }

    public void onClickServiceRestart(View view) {
        requestServiceRestart();
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

    public boolean isServiceEnabledInSettings() {
        ContentResolver contentResolver = getActivity().getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = getActivity().getPackageName();

        return !(enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName));
    }

    private void requestConfigurationCheck() {

        // Reset notification status list
        updateServiceRestartWarning(false);

        // Configuration check request
        Configuration.requestConfigurationCheck(getActivity());

    }

    private void requestServiceRestart() {

        // Service restart request
        Intent localIntent = NotificationToDoService.newRestartRequest();
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(localIntent);

    }

    private void updateServiceStatusSub(boolean state) {
        if (state) {
            ((Switch) getActivity().findViewById(R.id.switchService)).setChecked(true);
            ((TextView) getActivity().findViewById(R.id.textServiceStatusSub)).setText("Enabled");
        }
        else {
            ((Switch) getActivity().findViewById(R.id.switchService)).setChecked(false);
            ((TextView) getActivity().findViewById(R.id.textServiceStatusSub)).setText("Disabled");
        }
    }

    private void updateServiceRestartWarning(boolean state) {
        RelativeLayout layout = (RelativeLayout) getActivity().findViewById(R.id.layoutServiceRestartWarning);
        layout.setVisibility(state ? View.VISIBLE : View.GONE);
        layout.requestLayout();
    }

}
