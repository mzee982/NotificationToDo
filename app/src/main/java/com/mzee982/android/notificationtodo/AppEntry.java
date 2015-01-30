package com.mzee982.android.notificationtodo;

import android.graphics.drawable.Drawable;

public class AppEntry implements Comparable<AppEntry> {

    private Drawable mIcon;
    private String mLabel;
    private String mPackageName;
    private long mId;

    public AppEntry(Drawable icon, String label, String packageName) {
        mIcon = icon;
        mLabel = label;
        mPackageName = packageName;
        mId = hashCode();
    }

    @Override
    public int compareTo(AppEntry another) {
        return this.getLabel().compareTo(another.getLabel());
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof AppEntry) && (mPackageName.equals(((AppEntry) o).getPackageName()));
    }

    @Override
    public int hashCode() {
        return mPackageName.hashCode();
    }

    @Override
    public String toString() {
        return mLabel;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public String getLabel() {
        return mLabel;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public long getId() {
        return mId;
    }

}
