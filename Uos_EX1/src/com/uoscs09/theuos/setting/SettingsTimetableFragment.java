package com.uoscs09.theuos.setting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.PrefUtil;

public class SettingsTimetableFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {
	private AlertDialog timetableLimitPickerDialog;
	private int limit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActivity().getActionBar().setTitle(R.string.setting_timetable);
		addPreferencesFromResource(R.xml.prefrence_timetable);
		bindPreferenceSummaryToValue();
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		switch (preference.getTitleRes()) {
		case R.string.setting_timetable_limit:
			showNumberPicker();
			return true;
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
			int limit = sharedPreferences.getInt(key, 15);
			StringBuilder sb = new StringBuilder();
			sb.append(getString(R.string.setting_timetable_limit_desc));
			if (limit == 15) {
				sb.append("\n* 모든 교시가 표시됩니다.");
			} else {
				sb.append("\n* ").append(limit).append("교시 까지 표시됩니다.");
			}
			connectionPref.setSummary(sb.toString());
		}
	}

	/** 시간표 표시제한에 사용되는 NumberPicker를 보여준다. 값이 null이라면 초기화도 한다. */
	private void showNumberPicker() {
		if (timetableLimitPickerDialog == null) {
			NumberPicker np = new NumberPicker(getActivity());
			np.setMaxValue(PrefUtil.TIMETABLE_LIMIT_MAX);
			np.setMinValue(PrefUtil.TIMETABLE_LIMIT_MIN);
			np.setOnValueChangedListener(new OnValueChangeListener() {
				@Override
				public void onValueChange(NumberPicker picker, int oldVal,
						int newVal) {
					limit = newVal;
				}
			});
			timetableLimitPickerDialog = new AlertDialog.Builder(getActivity())
					.setView(np)
					.setTitle("표시 될 교시를 선택하세요.")
					.setPositiveButton(android.R.string.ok,
							new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									if (limit < PrefUtil.TIMETABLE_LIMIT_MIN) {
										limit = PrefUtil.TIMETABLE_LIMIT_MIN;
									} else if (limit > PrefUtil.TIMETABLE_LIMIT_MAX) {
										limit = PrefUtil.TIMETABLE_LIMIT_MAX;
									}
									Context context = getActivity();
									PrefUtil.getInstance(context)
											.put(PrefUtil.KEY_TIMETABLE_LIMIT,
													limit);
									AppUtil.showToast(context,
											String.valueOf(limit)
													+ "교시 까지 표시됩니다.", true);
									AppUtil.timetable_limit = limit;
									onSharedPreferenceChanged(
											getPreferenceScreen()
													.getSharedPreferences(),
											PrefUtil.KEY_TIMETABLE_LIMIT);
								}
							}).create();
		}
		timetableLimitPickerDialog.show();
	}
}
