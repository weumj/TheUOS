package com.uoscs09.theuos.setting;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.util.AppUtil;

public class SettingsWebPageFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActivity().getActionBar().setTitle(R.string.setting_web_page);
		addPreferencesFromResource(R.xml.prefrence_web_page);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		switch (preference.getTitleRes()) {
		case R.string.web_page_uos:
			startActivity(AppUtil.setWebPageIntent("http://m.uos.ac.kr/"));
			return true;
		case R.string.web_page_portal:
			startActivity(AppUtil.setWebPageIntent("http://portal.uos.ac.kr/"));
			return true;
		case R.string.web_page_club:
			startActivity(AppUtil.setWebPageIntent("http://club.uos.ac.kr/"));
			return true;
		case R.string.web_page_library:
			startActivity(AppUtil
					.setWebPageIntent("http://mlibrary.uos.ac.kr/"));
			return true;
		case R.string.web_page_square:
			startActivity(AppUtil
					.setWebPageIntent("http://m.cafe.daum.net/uosisthebest/"));
			return true;
		case R.string.web_page_uostime:
			startActivity(AppUtil.setWebPageIntent("http://uosti.me/"));
			return true;
		case R.string.web_page_uostable:
			startActivity(AppUtil.setWebPageIntent("http://u5s.kr/"));
			return true;
		default:
			return false;
		}
	}
}
