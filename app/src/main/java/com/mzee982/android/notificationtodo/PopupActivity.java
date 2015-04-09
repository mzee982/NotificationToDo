package com.mzee982.android.notificationtodo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

public class PopupActivity extends Activity implements PopupDialogFragment.PopupDialogListener {

    private static final String EXTRA_NOTIFICATION_DETAILS = "EXTRA_NOTIFICATION_DETAILS";
    private static final String TAG_POPUP_DIALOG = "TAG_POPUP_DIALOG";

    private NotificationDetails mNotificationDetails;
    private boolean mIsCanceled;
    private boolean mIsDone;

    public static Intent newIntent(Context context, NotificationDetails notificationDetails) {
        Intent popupIntent = new Intent(context, PopupActivity.class);
        popupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        popupIntent.putExtra(EXTRA_NOTIFICATION_DETAILS, notificationDetails);

        return popupIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //
        Intent intent = getIntent();

        if ((intent != null) && (intent.getExtras() != null)) {
            mNotificationDetails = (NotificationDetails) intent.getExtras().getParcelable(EXTRA_NOTIFICATION_DETAILS);
        }

        //
        mIsCanceled = false;
        mIsDone = false;

        //
        setContentView(R.layout.activity_popup);
    }

    @Override
    protected void onResume() {

        //
        PopupDialogFragment popupDialogFragment = PopupDialogFragment.newInstance(mNotificationDetails);
        popupDialogFragment.show(getFragmentManager(), TAG_POPUP_DIALOG);

        super.onResume();
    }

    @Override
    protected void onPause() {

        if (!isFinishing()) {
            PopupDialogFragment popupDialogFragment = (PopupDialogFragment) getFragmentManager().findFragmentByTag(TAG_POPUP_DIALOG);

            // Navigate off the dialog
            if ((popupDialogFragment != null) && (popupDialogFragment.getDialog() != null) && !mIsDone) {
                popupDialogFragment.getDialog().cancel();
            }

            // Dialog not present
            else {
                finish();
            }

        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        //
        sendPopupStatus();

        //
        super.onDestroy();
    }

    @Override
    public void onPopupDialogToDoClick(View view, String extraText) {
        mIsDone = true;

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.addCategory(Intent.CATEGORY_DEFAULT);
        sendIntent.putExtra(Intent.EXTRA_TEXT, extraText);
        sendIntent.setType("text/plain");

        switch (view.getId()) {
            case R.id.imageButtonAnyDo:
                sendIntent.setClassName("com.anydo", "com.anydo.activity.Main");
                break;
            case R.id.imageButtonGoogleKeep:
                sendIntent.setClassName("com.google.android.keep", "com.google.android.keep.activities.EditorActivity");
                break;
        }

        startActivity(sendIntent);
    }

    @Override
    public void onPopupDialogCancel() {
        mIsCanceled = true;
    }

    @Override
    public void onPopupDialogDismiss() {
        if (!isFinishing()) {
            finish();
        }
    }

    private void sendPopupStatus() {
        Intent localIntent = NotificationToDoService.newPopupStatusIntent(mNotificationDetails.getId(), mIsCanceled, mIsDone);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    @Override
    public void finish() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.finishAndRemoveTask();
        }
        else {
            super.finish();
        }
    }

}
