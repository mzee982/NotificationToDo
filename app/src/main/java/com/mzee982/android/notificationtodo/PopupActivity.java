package com.mzee982.android.notificationtodo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

public class PopupActivity extends Activity implements PopupDialogFragment.PopupDialogListener {

    private static final String EXTRA_PACKAGE_NAME = "EXTRA_PACKAGE_NAME";
    private static final String EXTRA_TITLE = "EXTRA_TITLE";
    private static final String EXTRA_TEXT = "EXTRA_TEXT";
    private static final String TAG_POPUP_DIALOG = "TAG_POPUP_DIALOG";

    String mExtraPackageName;
    String mExtraTitle;
    String mExtraText;

    public static Intent newIntent(Context context, String packageName, String title, String text) {
        Intent popupIntent = new Intent(context, PopupActivity.class);
        popupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        popupIntent.putExtra(EXTRA_PACKAGE_NAME, packageName);
        popupIntent.putExtra(EXTRA_TITLE, title);
        popupIntent.putExtra(EXTRA_TEXT, text);

        return popupIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //
        Intent intent = getIntent();

        if ((intent != null) && (intent.getExtras() != null)) {
            Bundle intentExtras = intent.getExtras();

            mExtraPackageName = intentExtras.getString(EXTRA_PACKAGE_NAME);
            mExtraTitle = intentExtras.getString(EXTRA_TITLE);
            mExtraText = intentExtras.getString(EXTRA_TEXT);
        }

        //
        setContentView(R.layout.activity_popup);
    }

    @Override
    protected void onResume() {

        //
        PopupDialogFragment popupDialogFragment = PopupDialogFragment.newInstance(mExtraPackageName, mExtraTitle, mExtraText);
        popupDialogFragment.show(getFragmentManager(), TAG_POPUP_DIALOG);

        super.onResume();
    }

    @Override
    protected void onPause() {
        PopupDialogFragment popupDialogFragment = (PopupDialogFragment) getFragmentManager().findFragmentByTag(TAG_POPUP_DIALOG);

        if ((popupDialogFragment != null) && (popupDialogFragment.getDialog() != null)) {
            popupDialogFragment.getDialog().dismiss();
        }
        else if (!isFinishing()) {
            finish();
        }

        super.onPause();
    }

    @Override
    public void onPopupDialogDismiss() {
        finish();
    }

    @Override
    public void onPopupDialogCancel() {
        //
    }

    @Override
    public void onPopupDialogToDoClick(View view) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.addCategory(Intent.CATEGORY_DEFAULT);

        sendIntent.putExtra(Intent.EXTRA_TEXT, mExtraTitle + " - " + mExtraText);
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

        finish();
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
