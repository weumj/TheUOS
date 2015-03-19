package com.uoscs09.theuos.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.util.PrefUtil;

public class SettingsTimetableFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefrence_timetable);
		bindPreferenceSummaryToValue();
	}

	@Override
	public void onResume() {
		super.onResume();
		ActionBarActivity activity = (ActionBarActivity) getActivity();
		activity.getSupportActionBar().setTitle(R.string.setting_timetable);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			@NonNull Preference preference) {
		switch (preference.getTitleRes()) {
		default:
			return false;
		}
	}

	private void bindPreferenceSummaryToValue() {
		SharedPreferences pref = getPreferenceScreen().getSharedPreferences();
		pref.registerOnSharedPreferenceChangeListener(this);
		String[] keys = { PrefUtil.KEY_TIMETABLE_LIMIT, };
		for (String key : keys) {
			onSharedPreferenceChanged(pref, key);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(PrefUtil.KEY_TIMETABLE_LIMIT)) {
			Preference connectionPref = findPreference(key);
			boolean limit = sharedPreferences.getBoolean(key, false);
			Context context = getActivity();
			if (context != null) {
				if (limit) {
					connectionPref
							.setSummary(R.string.setting_timetable_limit_desc_check);
				} else {
					connectionPref
							.setSummary(R.string.setting_timetable_limit_desc);
				}
			}
		}
	}
}
