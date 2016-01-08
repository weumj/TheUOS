package com.uoscs09.theuos2.setting;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.TrackerUtil;

public class SettingsWebPageFragment extends PreferenceFragmentCompat {

    private static final String TAG = "SettingsWebPageFragment";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TrackerUtil.getInstance(this).sendVisibleEvent(TAG);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.prefrence_web_page);
    }

    @Override
    public void onResume() {
        super.onResume();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity.getSupportActionBar() != null)
            activity.getSupportActionBar().setTitle(R.string.setting_web_page);
    }


    @Override
    public boolean onPreferenceTreeClick(@NonNull Preference preference) {

        CharSequence title = preference.getTitle();

        if (title.equals(getText(R.string.web_page_uos))) {
            startActivity(AppUtil.getWebPageIntent("http://m.uos.ac.kr/"));
            return true;

        } else if (title.equals(getText(R.string.web_page_portal))) {
            startActivity(AppUtil.getWebPageIntent("http://portal.uos.ac.kr/"));
            return true;

        } else if (title.equals(getText(R.string.web_page_club))) {
            startActivity(AppUtil.getWebPageIntent("http://club.uos.ac.kr/"));
            return true;

        } else if (title.equals(getText(R.string.web_page_library))) {
            startActivity(AppUtil.getWebPageIntent("http://mlibrary.uos.ac.kr/"));
            return true;

        } else if (title.equals(getText(R.string.web_page_square))) {
            startActivity(AppUtil.getWebPageIntent("http://m.cafe.daum.net/uosisthebest/"));
            return true;

        } else if (title.equals(getText(R.string.web_page_uostable))) {
            startActivity(AppUtil.getWebPageIntent("http://u5s.kr/"));
            return true;

        } else if (title.equals(getText(R.string.web_page_uostime))) {
            startActivity(AppUtil.getWebPageIntent("http://uosti.me/"));
            return true;

        }

        return false;

    }
}
