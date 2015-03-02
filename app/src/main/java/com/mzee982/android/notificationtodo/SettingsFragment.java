package com.mzee982.android.notificationtodo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Configuration mConfiguration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mConfiguration = new Configuration(getActivity());
        addPreferencesFromResource(R.xml.preferences);
        updateSummaries();
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        //
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        //
        mConfiguration.commit(getActivity());

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSummaries(sharedPreferences, key);
    }

    private void updateSummaries() {
        updateSummaries(null, null);
    }

    private void updateSummaries(SharedPreferences sharedPreferences, String key) {

        if (sharedPreferences ==  null) sharedPreferences = getPreferenceManager().getSharedPreferences();

        //
        String targetKey = (key != null) ? key : Configuration.PREF_KEY_POPUP_TRIGGER;

        if (targetKey.equals(Configuration.PREF_KEY_POPUP_TRIGGER)) {
            ListPreference popupTriggerPreference = (ListPreference) findPreference(targetKey);

            String prefValuePopupTriggerPosted = getString(R.string.pref_value_popup_trigger_posted);
            String prefValuePopupTriggerRemoved = getString(R.string.pref_value_popup_trigger_removed);
            String prefDefaultPopupTrigger = getString(R.string.pref_default_popup_trigger);
            String prefSummaryPopupTriggerPosted = getString(R.string.pref_summary_popup_trigger_posted);
            String prefSummaryPopupTriggerRemoved = getString(R.string.pref_summary_popup_trigger_removed);

            String prefValue = sharedPreferences.getString(targetKey, prefDefaultPopupTrigger);

            if (prefValuePopupTriggerPosted.equals(prefValue)) {
                popupTriggerPreference.setSummary(prefSummaryPopupTriggerPosted);
            }
            else if (prefValuePopupTriggerRemoved.equals(prefValue)) {
                popupTriggerPreference.setSummary(prefSummaryPopupTriggerRemoved);
            }
            else {
                popupTriggerPreference.setSummary("");
            }

        }

    }

}
