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
        GridLayout gridLayoutSelectedApplications = (GridLayout) getActivity().findViewById(R.id.gridLayoutSelectedApplications);
        gridLayoutSelectedApplications.removeAllViews();
        gridLayoutSelectedApplications.setColumnCount(1);

        // First grid item
        if (mSelectedApplicationsAdapter.getCount() > 0) {
            View gridItem = mSelectedApplicationsAdapter.getView(0, null, gridLayoutSelectedApplications);
            gridLayoutSelectedApplications.addView(gridItem);
        }

        //
        gridLayoutSelectedApplications.post(new Runnable() {
            @Override
            public void run() {
                //TODO Investigate getActivity() is null
                GridLayout gridLayoutSelectedApplications = (GridLayout) getActivity().findViewById(R.id.gridLayoutSelectedApplications);

                // Column count
                if (gridLayoutSelectedApplications.getChildCount() > 0) {
                    GridLayout layoutSelectedApplications = (GridLayout) getActivity().findViewById(R.id.gridLayoutSelectedApplications);
                    View child = gridLayoutSelectedApplications.getChildAt(0);
                    ViewGroup.MarginLayoutParams childLayoutParams = (ViewGroup.MarginLayoutParams) child.getLayoutParams();

                    int childWidth = child.getWidth();
                    int childMargin = childLayoutParams.leftMargin + childLayoutParams.rightMargin;
                    int layoutWidth = layoutSelectedApplications.getWidth();

                    layoutSelectedApplications.setColumnCount(layoutWidth / (childWidth + childMargin));
                }

                // More grid items
                for (int i = 1; i < mSelectedApplicationsAdapter.getCount(); i++) {
                    View gridItem = mSelectedApplicationsAdapter.getView(i, null, gridLayoutSelectedApplications);
                    gridLayoutSelectedApplications.addView(gridItem);
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
