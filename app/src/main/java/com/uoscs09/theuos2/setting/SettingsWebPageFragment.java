package com.uoscs09.theuos2.setting;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.TrackerUtil;

public class SettingsWebPageFragment extends PreferenceFragment {

    private static final String TAG = "SettingsWebPageFragment";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefrence_web_page);

        TrackerUtil.getInstance(this).sendVisibleEvent(TAG);
    }

    @Override
    public void onResume() {
        super.onResume();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.setting_web_page);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, @NonNull Preference preference) {

        switch (preference.getTitleRes()) {
            case R.string.web_page_uos:
                startActivity(AppUtil.getWebPageIntent("http://m.uos.ac.kr/"));
                return true;

            case R.string.web_page_portal:
                startActivity(AppUtil.getWebPageIntent("http://portal.uos.ac.kr/"));
                return true;

            case R.string.web_page_club:
                startActivity(AppUtil.getWebPageIntent("http://club.uos.ac.kr/"));
                return true;

            case R.string.web_page_library:
                startActivity(AppUtil.getWebPageIntent("http://mlibrary.uos.ac.kr/"));
                return true;

            case R.string.web_page_square:
                startActivity(AppUtil.getWebPageIntent("http://m.cafe.daum.net/uosisthebest/"));
                return true;

            case R.string.web_page_uostable:
                startActivity(AppUtil.getWebPageIntent("http://u5s.kr/"));
                return true;

            case R.string.web_page_uostime:
                startActivity(AppUtil.getWebPageIntent("http://uosti.me/"));
                return true;

            default:
                return false;

        }
    }
}
