package com.mzee982.android.notificationtodo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;

public class ChooseApplicationsDialogFragment extends DialogFragment
        implements SearchView.OnQueryTextListener, AdapterView.OnItemClickListener {

    private ChooseApplicationsDialogListener mListener;
    private ApplicationArrayAdapter mAdapter;
    private ListView mListApplication;
    private SearchView mSearchView;

    public interface ChooseApplicationsDialogListener {
        public AppList getAppList();
        public void onDialogPositiveClick(ArrayList<Long> selectedIds);
        public void onDialogNegativeClick();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Fragment fragment = null;

        try {
            fragment = getFragmentManager().findFragmentByTag(ApplicationsFragment.TAG);
            mListener = (ChooseApplicationsDialogListener) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException((fragment != null) ? fragment.toString() : "null"
                    + " must implement ChooseApplicationsDialogListener");
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (mAdapter != null) {
            mAdapter.saveState(outState);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_choose_applications, null);

        mListApplication = (ListView) layout.findViewById(R.id.listApplication);
        mListApplication.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
        mListApplication.setOnItemClickListener(this);

        mSearchView = (SearchView) layout.findViewById(R.id.searchView);
        mSearchView.setOnQueryTextListener(this);

        mAdapter = new ApplicationArrayAdapter(getActivity(), ApplicationArrayAdapter.MODE_LIST,
                                                mListener.getAppList().getList(),
                                                mListener.getAppList().getSelectedIds());
        mAdapter.restoreState(savedInstanceState);
        mListApplication.setAdapter(mAdapter);

        builder.setView(layout);

        builder.setTitle("Choose applications");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mListener.onDialogPositiveClick(mAdapter.getSelectedIds());
            }});
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mListener.onDialogNegativeClick();
            }});

        return builder.create();

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        mAdapter.getFilter().filter(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mAdapter.getFilter().filter(newText);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.toggleSelection(id);
    }

}
