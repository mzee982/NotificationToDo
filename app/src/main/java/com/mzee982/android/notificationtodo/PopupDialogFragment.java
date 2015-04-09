package com.mzee982.android.notificationtodo;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

public class PopupDialogFragment extends DialogFragment {

    public interface PopupDialogListener {
        public void onPopupDialogDismiss();
        public void onPopupDialogCancel();
        public void onPopupDialogToDoClick(View view, String extraText);
    }

    private class NotificationTextSelectionListener implements View.OnClickListener {
        private EditText mTargetEditText;

        public NotificationTextSelectionListener(EditText targetEditText) {
            mTargetEditText = targetEditText;
        }

        @Override
        public void onClick(View v) {
            if (v instanceof ViewGroup) {
                View childView = ((ViewGroup) v).getChildAt(0);

                if ((childView != null) && (childView instanceof TextView)) {
                    CharSequence notificationText = ((TextView) childView).getText();
                    Editable targetEditable = mTargetEditText.getEditableText();
                    int selectionStart = mTargetEditText.getSelectionStart();
                    int selectionEnd = mTargetEditText.getSelectionEnd();

                    targetEditable.replace(selectionStart, selectionEnd, notificationText);
                }
            }
        }

    }

    private static final String ARG_NOTIFICATION_DETAILS = "ARG_NOTIFICATION_DETAILS";

    private PopupDialogListener mListener;
    private NotificationDetails mNotificationDetails;

    public static PopupDialogFragment newInstance(NotificationDetails notificationDetails) {
        PopupDialogFragment popupDialogFragment = new PopupDialogFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_NOTIFICATION_DETAILS, notificationDetails);

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
        mNotificationDetails = (NotificationDetails) args.getParcelable(ARG_NOTIFICATION_DETAILS);

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
        EditText editTextNotification = (EditText) dialogView.findViewById(R.id.editTextNotification);

        //
        View notificationView = mNotificationDetails.inflateSelectableNotificationContent(getActivity(), new NotificationTextSelectionListener(editTextNotification));
        FrameLayout frameLayoutNotification = (FrameLayout) dialogView.findViewById(R.id.frameLayoutNotification);
        frameLayoutNotification.addView(notificationView);

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
        EditText editTextNotification = (EditText) getDialog().findViewById(R.id.editTextNotification);
        String extraText = editTextNotification.getText().toString();

        mListener.onPopupDialogToDoClick(view, extraText);

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
