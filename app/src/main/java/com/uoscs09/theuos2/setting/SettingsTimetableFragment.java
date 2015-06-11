package com.uoscs09.theuos2.setting;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.async.AsyncUtil;
import com.uoscs09.theuos2.tab.timetable.TimetableAlarmUtil;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.PrefUtil;
import com.uoscs09.theuos2.util.TrackerUtil;

public class SettingsTimetableFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    private static final String TAG = "SettingsTimetableFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefrence_timetable);
        bindPreferenceSummaryToValue();

        TrackerUtil.getInstance(this).sendVisibleEvent(TAG);
    }

    @Override
    public void onResume() {
        super.onResume();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.setting_timetable);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, @NonNull Preference preference) {
        switch (preference.getTitleRes()) {

            case R.string.setting_timetable_clear_alarm_title:
                final Dialog dialog = AppUtil.getProgressDialog(getActivity(), false, getString(R.string.progress_ongoing), null);
                dialog.show();

                TrackerUtil.getInstance(this).sendEvent(TAG, "clear timetable alarm");

                TimetableAlarmUtil.clearAllAlarmWithResult(getActivity(), new AsyncUtil.OnTaskFinishedListener<Boolean>() {
                    @Override
                    public void onTaskFinished(boolean isExceptionOccurred, Boolean data, Exception e) {
                        if (isExceptionOccurred || !data)
                            AppUtil.showToast(getActivity(), R.string.failed);
                        else
                            AppUtil.showToast(getActivity(), R.string.success);

                        dialog.dismiss();
                    }
                });
                return true;

            default:
                return false;
        }
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
        if (key.equals(PrefUtil.KEY_TIMETABLE_LIMIT)) {
            Preference connectionPref = findPreference(key);
            boolean limit = sharedPreferences.getBoolean(key, false);

            TrackerUtil.getInstance(this).sendEvent(TAG, key, "" + limit);

            connectionPref.setSummary(limit ? R.string.setting_timetable_limit_desc_check : R.string.setting_timetable_limit_desc);

        }
    }
}
