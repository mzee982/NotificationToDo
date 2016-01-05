package com.mzee982.android.notificationtodo;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;

import java.util.ArrayList;

public class ApplicationsFragment extends Fragment implements ChooseApplicationsDialogFragment.ChooseApplicationsDialogListener {

    public static final String TAG = "FRAGMENT_APPLICATIONS";

    private Configuration mConfiguration;
    private ApplicationArrayAdapter mSelectedApplicationsAdapter;
    private GridLayout mGridLayoutSelectedApplications;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mConfiguration = new Configuration(getActivity());

        mSelectedApplicationsAdapter = new ApplicationArrayAdapter(getActivity(),
                ApplicationArrayAdapter.MODE_GRID,
                mConfiguration.getAppList().getSelectedList(),
                null);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentApplications = inflater.inflate(R.layout.fragment_applications, container, false);

        ImageButton imageButtonApplicationsNew = (ImageButton) fragmentApplications.findViewById(R.id.imageButtonApplicationsNew);

        imageButtonApplicationsNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickApplicationsNew(v);
            }
        });

        mGridLayoutSelectedApplications = (GridLayout) fragmentApplications.findViewById(R.id.gridLayoutSelectedApplications);

        return fragmentApplications;
    }

    @Override
    public void onResume() {
        refreshSelectedApplicationsGrid();

        super.onResume();
    }

    public void onClickApplicationsNew(View view) {

        // Show application chooser dialog
        DialogFragment dialogFragment = new ChooseApplicationsDialogFragment();
        dialogFragment.show(getFragmentManager(), "chooseApplications");

    }

    @Override
    public void onDialogPositiveClick(ArrayList<Long> selectedIds) {
        mConfiguration.getAppList().setSelectedIds(selectedIds);

        mConfiguration.commit(getActivity());

        mSelectedApplicationsAdapter.clear();
        mSelectedApplicationsAdapter.addAll(mConfiguration.getAppList().getSelectedList());

        refreshSelectedApplicationsGrid();
    }

    @Override
    public void onDialogNegativeClick() {

    }

    public AppList getAppList() {
        return mConfiguration.getAppList();
    }

    private void refreshSelectedApplicationsGrid() {

        // Grid layout init
        mGridLayoutSelectedApplications.removeAllViews();
        mGridLayoutSelectedApplications.setColumnCount(1);

        // First grid item
        if (mSelectedApplicationsAdapter.getCount() > 0) {
            View gridItem = mSelectedApplicationsAdapter.getView(0, null, mGridLayoutSelectedApplications);
            mGridLayoutSelectedApplications.addView(gridItem);
        }

        //
        mGridLayoutSelectedApplications.post(new Runnable() {
            @Override
            public void run() {

                // Column count
                if (mGridLayoutSelectedApplications.getChildCount() > 0) {
                    View child = mGridLayoutSelectedApplications.getChildAt(0);
                    ViewGroup.MarginLayoutParams childLayoutParams = (ViewGroup.MarginLayoutParams) child.getLayoutParams();

                    int childWidth = child.getWidth();
                    int childMargin = childLayoutParams.leftMargin + childLayoutParams.rightMargin;
                    int layoutWidth = mGridLayoutSelectedApplications.getWidth();

                    mGridLayoutSelectedApplications.setColumnCount(layoutWidth / (childWidth + childMargin));
                }

                // More grid items
                for (int i = 1; i < mSelectedApplicationsAdapter.getCount(); i++) {
                    View gridItem = mSelectedApplicationsAdapter.getView(i, null, mGridLayoutSelectedApplications);
                    mGridLayoutSelectedApplications.addView(gridItem);
                }

            }
        });

    }

    private void requestConfigurationCheck() {

        // Configuration check request
        Intent localIntent = NotificationToDoService.newConfigurationCheckRequestIntent();
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(localIntent);

    }


}
