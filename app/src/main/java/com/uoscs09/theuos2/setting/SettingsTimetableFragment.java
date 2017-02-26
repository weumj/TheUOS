package com.uoscs09.theuos2.setting;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.util.PrefUtil;
import com.uoscs09.theuos2.util.TrackerUtil;

public class SettingsTimetableFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {
    private static final String TAG = "SettingsTimetableFragment";

    private TrackerUtil trackerUtil;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        trackerUtil = new TrackerUtil(getActivity());
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.prefrence_timetable);
        bindPreferenceSummaryToValue();
    }

    @Override
    public void onResume() {
        super.onResume();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.setting_timetable);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        trackerUtil = null;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
          /*
        switch (preference.getTitleRes()) {


            case R.string.setting_timetable_clear_alarm_title:
                final Dialog dialog = AppUtil.getProgressDialog(getActivity(), false, getString(R.string.progress_ongoing), null);
                dialog.show();

                TrackerUtil.getInstance(this).sendEvent(TAG, "clear timetable alarm");

                TimetableAlarmUtil.clearAllAlarmWithResult(getActivity(),
                        new Request.ResultListener<Boolean>() {
                            @Override
                            public void onResult(Boolean result) {
                                dialog.dismiss();
                                AppUtil.showToast(getActivity(), R.string.success);
                            }
                        },
                        new Request.ErrorListener() {
                            @Override
                            public void onError(Exception e) {
                                dialog.dismiss();
                                AppUtil.showToast(getActivity(), R.string.failed);
                            }
                        }
                );
                return true;

            default:
                return false;
        }
        */
        return super.onPreferenceTreeClick(preference);
    }


    private void bindPreferenceSummaryToValue() {
        SharedPreferences pref = getPreferenceScreen().getSharedPreferences();
        pref.registerOnSharedPreferenceChangeListener(this);
        String[] keys = {PrefUtil.KEY_TIMETABLE_LIMIT,};

        for (String key : keys) {
            onSharedPreferenceChanged(pref, key);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PrefUtil.KEY_TIMETABLE_LIMIT:
                Preference connectionPref = findPreference(key);
                boolean limit = sharedPreferences.getBoolean(key, false);

                if (trackerUtil == null) {
                    trackerUtil = new TrackerUtil(getActivity());
                }

                trackerUtil.sendEvent(TAG, key, "" + limit);

                connectionPref.setSummary(limit ? R.string.setting_timetable_limit_desc_check : R.string.setting_timetable_limit_desc);
                break;

            default:
                break;
        }

    }
}
