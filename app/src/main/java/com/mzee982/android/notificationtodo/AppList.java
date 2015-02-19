package com.mzee982.android.notificationtodo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class AppList {
    private static final String SHARED_PREFERENCES_NAME = AppList.class.getSimpleName();
    private static final String SHARED_PREFERENCES_KEY_SELECTED_IDS = "SHARED_PREFERENCES_KEY_SELECTED_IDS";
    private static final char DELIMITER = ',';

    private ArrayList<AppEntry> mList;
    private ArrayList<Long> mSelectedIds;

    public AppList(Context context) {
        mList = getLauncherApplicationList(context);
        mSelectedIds = new ArrayList<Long>();

        // Load selections
        load(context);

        // Sort by application labels
        sort();
    }

    private ArrayList<AppEntry> getLauncherApplicationList(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ArrayList<AppEntry> applicationList = new ArrayList<AppEntry>();

        Intent queryIntent = new Intent();
        queryIntent.setAction(Intent.ACTION_MAIN);
        queryIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> intentActivities = packageManager.queryIntentActivities(queryIntent, 0);

        for (ResolveInfo resolveInfo : intentActivities) {
            Drawable appIcon = resolveInfo.loadIcon(packageManager);
            CharSequence appLabelCS = resolveInfo.loadLabel(packageManager);
            String appLabel = appLabelCS != null ? appLabelCS.toString() : "";
            String appPackageName = resolveInfo.activityInfo.packageName;

            applicationList.add(new AppEntry(appIcon, appLabel, appPackageName));
        }

        return applicationList;
    }

    private void sort() {
        Collections.sort(mList);
    }

    public List<AppEntry> getList() {
        return mList;
    }

    public List<AppEntry> getSelectedList() {
        List<AppEntry> selectedList = new ArrayList<AppEntry>();

        for (AppEntry item : mList) {
            if (mSelectedIds.contains(item.getId())) {
                selectedList.add(item);
            }
        }

        return selectedList;
    }

    public ArrayList<Long> getSelectedIds() {
        return this.mSelectedIds;
    }

    public void setSelectedIds(ArrayList<Long> selectedIds) {
        mSelectedIds = selectedIds;
    }

    public void save(Context context) {
        String selectedIdsString = TextUtils.join(String.valueOf(DELIMITER), mSelectedIds);

        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SHARED_PREFERENCES_KEY_SELECTED_IDS, selectedIdsString);
        editor.commit();
    }

    private void load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String selectedIdsString = prefs.getString(SHARED_PREFERENCES_KEY_SELECTED_IDS, "");

        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(DELIMITER);
        splitter.setString(selectedIdsString);

        mSelectedIds.clear();

        while (splitter.hasNext()) {
            String token = splitter.next();
            mSelectedIds.add(Long.parseLong(token));
        }
    }

    public boolean isPackageSelected(String packageName) {
        boolean found = false;
        Iterator<AppEntry> iterator = mList.iterator();

        while (iterator.hasNext() && !found) {
            AppEntry item = iterator.next();

            String itemPackageName = item.getPackageName();
            long itemId = item.getId();

            boolean foundPackage = itemPackageName.equals(packageName);
            boolean foundId = mSelectedIds.contains(itemId);

            found = foundPackage && foundId;
        }

        return found;
    }

}
