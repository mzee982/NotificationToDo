package com.mzee982.android.notificationtodo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ApplicationArrayAdapter extends ArrayAdapter<AppEntry> {

    public static final int MODE_LIST = 1;
    public static final int MODE_GRID = 2;
    private static final String APPLICATION_ARRAY_ADAPTER_STATE_SELECTION = "APPLICATION_ARRAY_ADAPTER_STATE_SELECTION";

    private int mMode;
    private final LayoutInflater mInflater;
    private ArrayList<Long> mSelectedIds;

    public ApplicationArrayAdapter(Context context, int mode, List<AppEntry> objects, ArrayList<Long> selectedIds) {
        super(context, R.layout.list_item_application, objects);

        mMode = mode;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (selectedIds != null) {
            mSelectedIds = (ArrayList<Long>) selectedIds.clone();
        }

        else {
            mSelectedIds = new ArrayList<Long>();
        }
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mMode == MODE_LIST) {
            return getViewForList(position, convertView, parent);
        }

        else if (mMode == MODE_GRID) {
            return getViewForGrid(position, convertView, parent);
        }

        else {
            return null;
        }
    }

    public View getViewForList(int position, View convertView, ViewGroup parent) {
        CheckableRelativeLayout view;
        ImageView icon;
        CheckedTextView text;

        if (convertView == null) {
            view = (CheckableRelativeLayout) mInflater.inflate(R.layout.list_item_application, parent, false);
        } else {
            view = (CheckableRelativeLayout) convertView;
        }

        icon = (ImageView) view.findViewById(android.R.id.icon);
        text = (CheckedTextView) view.findViewById(android.R.id.text1);
        view.setCheckableView(text);

        AppEntry item = getItem(position);

        icon.setImageDrawable(item.getIcon());
        text.setText(item.getLabel());
        view.setChecked(mSelectedIds.contains(new Long(item.getId())));

        return view;
    }

    public View getViewForGrid(int position, View convertView, ViewGroup parent) {
        ImageView icon;
        TextView text;

        if (convertView == null) {
            convertView = (RelativeLayout) mInflater.inflate(R.layout.grid_item_application, parent, false);
        }

        icon = (ImageView) convertView.findViewById(android.R.id.icon);
        text = (TextView) convertView.findViewById(android.R.id.text1);

        AppEntry item = getItem(position);

        icon.setImageDrawable(item.getIcon());
        text.setText(item.getLabel());

        return convertView;
    }

    public void toggleSelection(long id) {
        Long idObj = new Long(id);
        int index = mSelectedIds.indexOf(idObj);

        // Deselect
        if (index >= 0) {
            mSelectedIds.remove(index);
        }

        // Select
        else {
            mSelectedIds.add(idObj);
        }

        notifyDataSetChanged();
    }

    public ArrayList<Long> getSelectedIds() {
        return mSelectedIds;
    }

    public void saveState(Bundle bundle) {
        int size = mSelectedIds.size();
        long[] selectedIdArray = new long[size];

        for (int i = 0; i < size; i++) {
            selectedIdArray[i] = mSelectedIds.get(i);
        }

        bundle.putLongArray(APPLICATION_ARRAY_ADAPTER_STATE_SELECTION, selectedIdArray);
    }

    public void restoreState(Bundle bundle) {
        long[] selectedIdArray = null;

        if (bundle != null) {
            selectedIdArray = bundle.getLongArray(APPLICATION_ARRAY_ADAPTER_STATE_SELECTION);
        }

        if (selectedIdArray != null) {
            mSelectedIds.clear();

            for (int i = 0; i < selectedIdArray.length; i++) {
                mSelectedIds.add(new Long(selectedIdArray[i]));
            }
        }
    }

}
