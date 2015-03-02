package com.mzee982.android.notificationtodo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

public class Configuration {
    private static final String PREF_KEY_VERSION = "pref_key_version";
    private static final String PREF_KEY_SERVICE_RUN_IN_FOREGROUND = "pref_key_service_run_in_foreground";
    public static final String PREF_KEY_POPUP_TRIGGER = "pref_key_popup_trigger";

    private int mVersion;
    private boolean mServiceRunInForeground;
    private String mPopupTrigger;
    private AppList mAppList;

    public static void requestConfigurationCheck(Context context) {

        // Configuration check request
        Intent localIntent = NotificationToDoService.newConfigurationCheckRequestIntent();
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);

    }

    public Configuration(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        mVersion = getPersistedVersion(context);
        mServiceRunInForeground = sharedPreferences.getBoolean(Configuration.PREF_KEY_SERVICE_RUN_IN_FOREGROUND, true);
        mPopupTrigger = sharedPreferences.getString(Configuration.PREF_KEY_POPUP_TRIGGER, context.getString(R.string.pref_default_popup_trigger));
        mAppList = new AppList(context);
    }

    private void increaseVersion(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        int version = preferences.getInt(PREF_KEY_VERSION, 0);
        version++;

        editor.putInt(PREF_KEY_VERSION, version);
        editor.apply();
    }

    private int getPersistedVersion(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int persistedVersion = preferences.getInt(PREF_KEY_VERSION, 0);
        return persistedVersion;
    }

    public boolean isObsolete(Context context) {
        int persistedVersion = getPersistedVersion(context);
        return mVersion < persistedVersion;
    }

    private boolean isChanged(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean changed = false;

        boolean persistedServiceRunInForeground = sharedPreferences.getBoolean(Configuration.PREF_KEY_SERVICE_RUN_IN_FOREGROUND, true);
        String persistedPopupTrigger = sharedPreferences.getString(Configuration.PREF_KEY_POPUP_TRIGGER, context.getString(R.string.pref_default_popup_trigger));
        boolean appListChanged = mAppList.isChanged(context);

        if ((mServiceRunInForeground != persistedServiceRunInForeground)
            || !(mPopupTrigger.equals(persistedPopupTrigger))
            || appListChanged) {
            changed = true;
        }

        return changed;
    }

    public void commit(Context context) {

        //
        if (isChanged(context)) increaseVersion(context);

        //
        mAppList.save(context);

        //
        requestConfigurationCheck(context);

    }

    public int getVersion() {
        return mVersion;
    }

    public boolean isServiceRunInForeground() {
        return mServiceRunInForeground;
    }

    public String getPopupTrigger() {
        return mPopupTrigger;
    }

    public AppList getAppList() {
        return mAppList;
    }

}
