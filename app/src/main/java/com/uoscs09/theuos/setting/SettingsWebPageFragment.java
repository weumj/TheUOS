package com.uoscs09.theuos.setting;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.util.AppUtil;

public class SettingsWebPageFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefrence_web_page);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBarActivity activity = (ActionBarActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.setting_web_page);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, @NonNull Preference preference) {

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
                startActivity(AppUtil.setWebPageIntent("http://mlibrary.uos.ac.kr/"));
                return true;

            case R.string.web_page_square:
                startActivity(AppUtil.setWebPageIntent("http://m.cafe.daum.net/uosisthebest/"));
                return true;

            case R.string.web_page_uostable:
                startActivity(AppUtil.setWebPageIntent("http://u5s.kr/"));
                return true;

            case R.string.web_page_uostime:
                startActivity(AppUtil.setWebPageIntent("http://uosti.me/"));
                return true;

            default:
                return false;

        }
    }
}
