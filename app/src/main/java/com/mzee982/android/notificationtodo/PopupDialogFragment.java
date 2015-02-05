package com.mzee982.android.notificationtodo;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class PopupDialogFragment extends DialogFragment {

    public interface PopupDialogListener {
        public void onPopupDialogDismiss();
        public void onPopupDialogCancel();
        public void onPopupDialogToDoClick(View view);
    }

    public static final String ARG_PACKAGE_NAME = "ARG_PACKAGE_NAME";
    public static final String ARG_TITLE = "ARG_TITLE";
    public static final String ARG_TEXT = "ARG_TEXT";

    private PopupDialogListener mListener;
    private String mPackageName;
    private String mTitle;
    private String mText;

    public static PopupDialogFragment newInstance(String packageName, String title, String text) {
        PopupDialogFragment popupDialogFragment = new PopupDialogFragment();

        Bundle args = new Bundle();
        args.putString(ARG_PACKAGE_NAME, packageName);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_TEXT, text);

        popupDialogFragment.setArguments(args);

        return popupDialogFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the listener so we can send events to the host
            mListener = (PopupDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement PopupDialogListener");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //
        Bundle args = getArguments();
        mPackageName = args.getString(ARG_PACKAGE_NAME);
        mTitle = args.getString(ARG_TITLE);
        mText = args.getString(ARG_TEXT);

        //
        int style = DialogFragment.STYLE_NORMAL;
        int theme = R.style.PopupTheme;

        setStyle(style, theme);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //
        getDialog().getWindow().setGravity(Gravity.BOTTOM);

        //
        View dialogView = inflater.inflate(R.layout.dialog_popup, container, false);

        //
        TextView textViewNotification = (TextView) dialogView.findViewById(R.id.textViewNotification);

        if (mPackageName != null) {
            textViewNotification.setText(mPackageName + "\n" + mTitle + "\n" + mText);
        }
        else {
            textViewNotification.setText("");
        }

        //
        try {
            Drawable anyDoIcon = getActivity().getPackageManager().getApplicationIcon("com.anydo");
            Drawable googleKeepIcon = getActivity().getPackageManager().getApplicationIcon("com.google.android.keep");

            ImageButton buttonCancel = (ImageButton) dialogView.findViewById(R.id.imageButtonCancel);
            ImageButton buttonAnyDo = (ImageButton) dialogView.findViewById(R.id.imageButtonAnyDo);
            ImageButton buttonGoogleKeep = (ImageButton) dialogView.findViewById(R.id.imageButtonGoogleKeep);

            buttonAnyDo.setImageDrawable(anyDoIcon);
            buttonGoogleKeep.setImageDrawable(googleKeepIcon);

            buttonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCancelButton(getDialog());
                }
            });
            buttonAnyDo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onToDo(v, getDialog());
                }
            });
            buttonGoogleKeep.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onToDo(v, getDialog());
                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return dialogView;
    }

    public void onCancelButton(DialogInterface dialog) {
        dialog.dismiss();
    }

    public void onToDo(View view, DialogInterface dialog) {
        mListener.onPopupDialogToDoClick(view);

        dialog.dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        mListener.onPopupDialogCancel();

        super.onCancel(dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mListener.onPopupDialogDismiss();

        super.onDismiss(dialog);
    }

}
