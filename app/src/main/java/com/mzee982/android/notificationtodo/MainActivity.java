package com.mzee982.android.notificationtodo;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //
        setContentView(R.layout.activity_main);

        //
        FragmentManager fragmentManager = getFragmentManager();

        // Existing fragments
        ServiceStatusFragment serviceStatusFragment = (ServiceStatusFragment) fragmentManager.findFragmentByTag(ServiceStatusFragment.TAG);
        ApplicationsFragment applicationsFragment = (ApplicationsFragment) fragmentManager.findFragmentByTag(ApplicationsFragment.TAG);

        // Add/Remove fragments
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Service Status Fragment
        if (serviceStatusFragment != null) fragmentTransaction.remove(serviceStatusFragment);
        serviceStatusFragment = new ServiceStatusFragment();
        fragmentTransaction.add(R.id.fragmentContainer, serviceStatusFragment, ServiceStatusFragment.TAG);

        // Applications Fragment
        if (applicationsFragment != null) fragmentTransaction.remove(applicationsFragment);
        applicationsFragment = new ApplicationsFragment();
        fragmentTransaction.add(R.id.fragmentContainer, applicationsFragment, ApplicationsFragment.TAG);

        fragmentTransaction.commit();

    }

    @Override
    protected void onResume() {
        FragmentManager fragmentManager = getFragmentManager();

        ServiceStatusFragment serviceStatusFragment = (ServiceStatusFragment) fragmentManager.findFragmentByTag(ServiceStatusFragment.TAG);
        NotificationStatusFragment notificationStatusFragment = (NotificationStatusFragment) fragmentManager.findFragmentByTag(NotificationStatusFragment.TAG);

        // Add Notification Status Fragment
        if ((serviceStatusFragment != null) && (serviceStatusFragment.isServiceEnabledInSettings())) {
            if (notificationStatusFragment == null) {
                notificationStatusFragment = new NotificationStatusFragment();
                fragmentManager.beginTransaction().add(R.id.fragmentContainer, notificationStatusFragment, NotificationStatusFragment.TAG).commit();
            }
        }

        // Remove Notification Status Fragment
        else {
            if (notificationStatusFragment != null) {
                fragmentManager.beginTransaction().remove(notificationStatusFragment).commit();
            }
        }

        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
