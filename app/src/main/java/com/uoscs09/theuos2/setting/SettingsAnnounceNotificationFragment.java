package com.uoscs09.theuos2.setting;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.widget.TimePicker;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.PrefUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.util.Calendar;

public class SettingsAnnounceNotificationFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    private TimePickerDialog timePicker;
    private boolean isAccepted = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefrence_announce_noti);
        bindPreferenceSummaryToValue();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, @NonNull Preference preference) {
        switch (preference.getTitleRes()) {
            case R.string.setting_check_announce_service:
                AppUtil.startOrStopServiceAnounce(getActivity());
                return true;

            case R.string.setting_noti_time:
                showTimePicker();
                return true;

            case R.string.announce_keyword:
                //if (!isNotiKeywordDialogSetting) {
                //	EditTextPreference prefKeyword = (EditTextPreference) preference;

                //AppUtil.setDialogMaterial(prefKeyword.getDialog(), getActivity());
                //	isNotiKeywordDialogSetting = true;
                //}
                return false;

            default:
                return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBarActivity activity = (ActionBarActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.setting_announce_noti_frag_title1);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    private void bindPreferenceSummaryToValue() {
        SharedPreferences pref = getPreferenceScreen().getSharedPreferences();
        pref.registerOnSharedPreferenceChangeListener(this);

        String[] keys = {PrefUtil.KEY_KEYWORD_ANOUNCE, PrefUtil.KEY_CHECK_ANOUNCE_SERVICE, PrefUtil.KEY_NOTI_TIME};
        for (String key : keys) {
            onSharedPreferenceChanged(pref, key);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PrefUtil.KEY_KEYWORD_ANOUNCE: {
                Preference connectionPref = findPreference(key);

                String desc = sharedPreferences.getString(key, StringUtil.NULL);
                if (desc.trim().equals(StringUtil.NULL)) {
                    connectionPref.setSummary(R.string.setting_announce_keyword_desc);
                } else {
                    connectionPref.setSummary(sharedPreferences.getString(key, StringUtil.NULL) + "\n가(이) 포함된 공지사항을 검색합니다.");
                }

            }
            break;


            case PrefUtil.KEY_NOTI_TIME: {
                Preference connectionPref = findPreference(key);
                
                int hour = sharedPreferences.getInt(StringUtil.STR_HOUR, -1);
                int min = sharedPreferences.getInt(StringUtil.STR_MIN, -1);

                if (hour == -1 && min == -1) {
                    connectionPref.setSummary(R.string.setting_noti_time_desc);
                } else {
                    String am_pm;
                    if (hour < 12) {
                        am_pm = StringUtil.STR_AM;
                    } else {
                        am_pm = StringUtil.STR_PM;
                        if (hour != 12)
                            hour -= 12;
                    }
                    String text = am_pm + String.valueOf(hour) + "시 " + String.valueOf(min) + "분에 알려줍니다.";
                    connectionPref.setSummary(text);
                    AppUtil.startOrStopServiceAnounce(getActivity());
                }

            }
            break;


            case PrefUtil.KEY_CHECK_ANOUNCE_SERVICE: {

                findPreference(key).setSummary(sharedPreferences.getBoolean(key, false) ? R.string.setting_check_announce_service_desc_enable : R.string.setting_check_announce_service_desc_disable);

                AppUtil.startOrStopServiceAnounce(getActivity());

            }
            break;


        }
    }

    private void showTimePicker() {
        if (timePicker == null) {
            final Calendar c = Calendar.getInstance();
            final Activity activity = getActivity();
            final PrefUtil pref = PrefUtil.getInstance(activity);

            int temp = pref.get(StringUtil.STR_HOUR, -1);
            int hour = temp == -1 ? c.get(Calendar.HOUR_OF_DAY) : temp;
            temp = pref.get(StringUtil.STR_MIN, -1);
            int min = temp == -1 ? c.get(Calendar.MINUTE) : temp;

            timePicker = new TimePickerDialog(activity,
                    new OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            if (!isAccepted)
                                return;

                            int pre_h = pref.get(StringUtil.STR_HOUR, -1);
                            int pre_m = pref.get(StringUtil.STR_MIN, -1);

                            if (pre_h == hourOfDay && pre_m == minute) {
                                AppUtil.showToast(activity, R.string.setting_noti_apply_not_commit);
                                return;
                            }

                            pref.put(StringUtil.STR_HOUR, hourOfDay);
                            pref.put(StringUtil.STR_MIN, minute);

                            String am_pm;
                            if (hourOfDay < 12) {
                                am_pm = StringUtil.STR_AM;
                            } else {
                                am_pm = StringUtil.STR_PM;
                                if (hourOfDay != 12)
                                    hourOfDay -= 12;
                            }

                            AppUtil.showToast(activity, "알림 시간이 \"" + am_pm + hourOfDay + "시 " + (minute < 10 ? '0'
                                    : StringUtil.NULL) + minute + "분\" 으로 " + getText(R.string.setting_confirm));

                            onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences(), PrefUtil.KEY_NOTI_TIME);
                        }
                    }, hour, min, true);

            timePicker.setTitle(R.string.setting_noti_time);
            timePicker.setCancelable(true);
            timePicker.setButton(DialogInterface.BUTTON_POSITIVE, getText(android.R.string.ok),
                    new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isAccepted = true;
                        }
                    });
            timePicker.setButton(DialogInterface.BUTTON_NEGATIVE, getText(android.R.string.no),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isAccepted = false;
                        }
                    });
            timePicker.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    isAccepted = false;
                }
            });

        }
        timePicker.show();
    }
}
