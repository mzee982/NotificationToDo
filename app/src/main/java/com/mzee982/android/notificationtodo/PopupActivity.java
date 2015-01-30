package com.mzee982.android.notificationtodo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


public class PopupActivity extends Activity {

    String mExtraPackageName;
    String mExtraTitle;
    String mExtraText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        params.gravity = Gravity.BOTTOM;

        LayoutInflater inflater = getLayoutInflater();
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.activity_popup, null);

        Intent intent = getIntent();

        if ((intent != null) && (intent.getExtras() != null)) {
            Bundle intentExtras = intent.getExtras();

            mExtraPackageName = intentExtras.getString("PACKAGE_NAME");
            mExtraTitle = intentExtras.getString("TITLE");
            mExtraText = intentExtras.getString("TEXT");
        }

        try {
            Drawable anyDoIcon = getPackageManager().getApplicationIcon("com.anydo");
            Drawable googleKeepIcon = getPackageManager().getApplicationIcon("com.google.android.keep");

            ImageButton buttonAnyDo = (ImageButton) layout.findViewById(R.id.imageButtonAnyDo);
            ImageButton buttonGoogleKeep = (ImageButton) layout.findViewById(R.id.imageButtonGoogleKeep);

            buttonAnyDo.setImageDrawable(anyDoIcon);
            buttonGoogleKeep.setImageDrawable(googleKeepIcon);

            buttonAnyDo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickToDo(v);
                }
            });
            buttonGoogleKeep.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickToDo(v);
                }
            });

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        setContentView(layout, params);
    }

    @Override
    protected void onResume() {

        // Update notification info
        TextView textViewNotification = (TextView) findViewById(R.id.textViewNotification);

        if (mExtraPackageName != null) {

            textViewNotification.setText(mExtraPackageName + "\n" + mExtraTitle + "\n" + mExtraText);

        }
        else {
            textViewNotification.setText("");
        }

        super.onResume();
    }

    public void onClickCancel(View view) {
        finish();
    }

    public void onClickToDo(View view) {
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

}
