package com.app.random.backapp.Fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.app.random.backapp.R;


public class SettingsFragment extends PreferenceFragment  {
    private final static String TAG = "SettingsFragment";


    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.activity_settings);

    }


//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        switch (key) {
//            case Keys.PREF_VIEWTYPE: {
//                // User change the view type need to update the UI
//                Log.d(TAG, "down User change to: " + SharedPrefsUtils.getStringPreference(getActivity().getBaseContext(), Keys.PREF_VIEWTYPE));
//
//                break;
//            }
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();
//        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
//        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}
