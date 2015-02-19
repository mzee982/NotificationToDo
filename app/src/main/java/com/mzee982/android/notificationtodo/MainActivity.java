package com.mzee982.android.notificationtodo;

import android.app.DialogFragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.Switch;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends ActionBarActivity implements ChooseApplicationsDialogFragment.ChooseApplicationsDialogListener {

    private AppList mAppList;
    private ApplicationArrayAdapter mSelectedApplicationsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAppList = new AppList(this);

        mSelectedApplicationsAdapter = new ApplicationArrayAdapter(this,
                                                    ApplicationArrayAdapter.MODE_GRID,
                                                    mAppList.getSelectedList(),
                                                    null);
    }

    @Override
    protected void onResume() {

        /*
         * Update service status
         */

        ContentResolver contentResolver = getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = getPackageName();
        boolean serviceStatus = !(enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName));

        if (serviceStatus) {
            ((Switch) findViewById(R.id.switchService)).setChecked(true);
            ((TextView) findViewById(R.id.textStatusSub)).setText("Enabled");
        }
        else {
            ((Switch) findViewById(R.id.switchService)).setChecked(false);
            ((TextView) findViewById(R.id.textStatusSub)).setText("Disabled");
        }

        /*
         * Selected applications
         */

        GridView gridSelectedApplications = (GridView) findViewById(R.id.gridSelectedApplications);
        gridSelectedApplications.setAdapter(mSelectedApplicationsAdapter);

        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickSwitchService(View view) {

        // Go to Notification Access Settings
        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));

    }

    public void onClickApplicationsNew(View view) {

        // Show application chooser dialog
        DialogFragment dialogFragment = new ChooseApplicationsDialogFragment();
        dialogFragment.show(getFragmentManager(), "chooseApplications");

    }

    @Override
    public void onDialogPositiveClick(ArrayList<Long> selectedIds) {
        mAppList.setSelectedIds(selectedIds);
        mAppList.save(this);

        Intent localIntent = NotificationToDoService.newAppListRefreshIntent();
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

        mSelectedApplicationsAdapter.clear();
        mSelectedApplicationsAdapter.addAll(mAppList.getSelectedList());
    }

    @Override
    public void onDialogNegativeClick() {

    }

    public void onTestNotificationClick(View view) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Notification To Do")
                .setContentText(DateFormat.getDateTimeInstance().format(new Date()))
                .setAutoCancel(true);

        Intent notificationIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    public AppList getAppList() {
        return mAppList;
    }

}
