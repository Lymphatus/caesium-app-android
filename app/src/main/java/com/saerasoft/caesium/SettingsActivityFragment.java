package com.saerasoft.caesium;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

public class SettingsActivityFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public SettingsActivityFragment() {

    }

    @SuppressWarnings("ResourceType")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.fragment_settings);

        //Get the current version ready to be displayed
        String appVersion;
        try {
            appVersion = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            appVersion = "";
        }
        //Write the version summary
        findPreference(SettingsActivity.KEY_INFO_VERSION).setSummary(appVersion);

        //Compression level summary
        ListPreference compressionLevelListPreference = (ListPreference) findPreference(SettingsActivity.KEY_COMPRESSION_LEVEL);
        findPreference(SettingsActivity.KEY_COMPRESSION_LEVEL).setSummary(compressionLevelListPreference.getEntry());

        /* --- Listeners --- */

        //Clear Database
        findPreference(SettingsActivity.KEY_MAINTENANCE_CLEAR_DATABASE).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ClearDatabaseDialogFragment dialog = new ClearDatabaseDialogFragment();
                dialog.show(getFragmentManager(), "ClearDatabaseDialog");
                return false;
            }
        });

        /* --- End of listeners --- */
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("PREF", "ListenerCalled with " + key.toString());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (key.equals(SettingsActivity.KEY_COMPRESSION_LEVEL)) {
            ListPreference compressionTypePref = (ListPreference) findPreference(key);
            // Set summary to be the user-description for the selected value
            compressionTypePref.setSummary(compressionTypePref.getEntry());
            editor.putInt(SettingsActivity.KEY_COMPRESSION_LEVEL, Integer.parseInt(compressionTypePref.getValue()));
        }
    }
}