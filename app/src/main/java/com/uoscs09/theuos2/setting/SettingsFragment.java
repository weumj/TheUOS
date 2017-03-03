package com.uoscs09.theuos2.setting;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;
import android.view.View;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.common.PieProgressDrawable;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.AppUtil.AppTheme;
import com.uoscs09.theuos2.util.PrefHelper;
import com.uoscs09.theuos2.util.PrefUtil;
import com.uoscs09.theuos2.util.TrackerUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import mj.android.utils.task.Tasks;
import rx.Subscription;

/**
 * 메인 설정화면을 나타내는 {@code PreferenceFragment}
 */
public class SettingsFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {
    private static final String TAG = "SettingsFragment";
    private AlertDialog mThemeSelectorDialog;

    private FragmentManager.OnBackStackChangedListener onBackStackChangedListener = () -> {
        if (getFragmentManager() != null) {
            if (getFragmentManager().getBackStackEntryCount() < 1) {
                getFragmentManager().beginTransaction()
                        .show(SettingsFragment.this)
                        .commit();
            }
        }
    };


    private TrackerUtil trackerUtil;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        trackerUtil = new TrackerUtil(getContext());

        super.onCreate(savedInstanceState);

        // Fragment가 재생성 되었을 때, 표시 화면을 정확히 복구하기 위한 설정
        getFragmentManager().addOnBackStackChangedListener(onBackStackChangedListener);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.prefrence);
        bindPreferenceSummaryToValue();
    }


    private void changeFragment(Class<? extends Fragment> clazz) {
        ((SettingActivity) getActivity()).restoreToolbar();

        getFragmentManager().beginTransaction()
                .hide(this)
                .add(android.R.id.tabcontent, Fragment.instantiate(getActivity(), clazz.getName()), "front")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onPreferenceTreeClick(@NonNull Preference preference) {

        CharSequence title = preference.getTitle();

        if (TextUtils.isEmpty(title))
            return false;

        if (title.equals(getText(R.string.setting_order))) {
            changeFragment(SettingsOrderFragment.class);
            return true;
        } else if (title.equals(getText(R.string.setting_theme))) {
            showThemeDialog();
            return true;
        /*}else if (title.equals(getText(R.string.setting_announce_except_type_notice))) {
            TrackerUtil.getInstance(this).sendEvent(TAG, "announce_except_type_notice");
            return true;
            */
        } else if (title.equals(getText(R.string.setting_delete_cache))) {
            trackerUtil.sendEvent(TAG, "delete cache");
            deleteCache();
            return true;

        } else if (title.equals(getText(R.string.setting_timetable))) {
            changeFragment(SettingsTimetableFragment.class);
            return true;

        } else if (title.equals(getText(R.string.setting_building_room_sub_title))) {
            downloadBuildings();
            return true;

        } else if (title.equals(getText(R.string.setting_save_doc_sub_title))) {
            trackerUtil.sendEvent(TAG, "change directory", "text");
            SettingsFileSelectDialogFragment f = new SettingsFileSelectDialogFragment();
            f.setSavePathKey(preference.getKey());
            f.setTitle(title);
            f.show(getFragmentManager(), null);
            return true;

        } else if (title.equals(getText(R.string.setting_save_image_sub_title))) {
            trackerUtil.sendEvent(TAG, "change directory", "image");
            SettingsFileSelectDialogFragment f = new SettingsFileSelectDialogFragment();
            f.setSavePathKey(preference.getKey());
            f.setTitle(title);
            f.show(getFragmentManager(), null);
            return true;

        } else if (title.equals(getText(R.string.setting_web_page))) {
            changeFragment(SettingsWebPageFragment.class);
            return true;

        }
        return false;
    }

    private void downloadBuildings() {
        Dialog dialog = AppUtil.getProgressDialog(getActivity());
        dialog.show();

        Subscription subscribe = AppRequests.Buildings.buildingRooms(true)
                .subscribe(room -> {
                            onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences(), PrefUtil.KEY_BUILDINGS_FETCH_TIME);
                            AppUtil.showToast(getActivity(), R.string.finish_update);
                        },
                        throwable -> {
                            AppUtil.showErrorToast(getActivity(), throwable, true);
                            dialog.dismiss();
                            dialog.setOnCancelListener(null);
                        },
                        () -> {
                            dialog.dismiss();
                            dialog.setOnCancelListener(null);
                        });

        dialog.setOnCancelListener(dialog1 -> subscribe.unsubscribe());
    }


    /**
     * 테마를 선택하는 dialog 를 보여준다. dialog 가 null 일시 초기화도 같이한다.
     */
    private void showThemeDialog() {
        if (mThemeSelectorDialog == null) {
            mThemeSelectorDialog = new AlertDialog.Builder(getActivity())
                    .setIconAttribute(R.attr.theme_ic_action_image_palette)
                    .setTitle(R.string.setting_plz_select_theme)
                    .setAdapter(new ThemeSelectAdapter(getActivity()), (dialog, i) -> {
                        int originalValue = PrefHelper.Screens.getAppTheme().ordinal();

                        if (originalValue != i) {

                            trackerUtil.sendEvent(TAG, "apply theme", AppTheme.values()[i].name(), i);

                            PrefHelper.Screens.putAppTheme(i);
                            onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences(), PrefUtil.KEY_THEME);
                            getActivity().setResult(AppUtil.RELAUNCH_ACTIVITY);
                        }

                        mThemeSelectorDialog.dismiss();
                    })
                    .create();
        }
        mThemeSelectorDialog.show();
    }

    private static class ThemeSelectAdapter extends AbsArrayAdapter<AppTheme, ViewHolder> {
        ThemeSelectAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1, AppTheme.values());
        }

        @Override
        public void onBindViewHolder(int position, SettingsFragment.ViewHolder holder) {
            holder.setView(getItem(position));
        }

        @Override
        public SettingsFragment.ViewHolder onCreateViewHolder(View convertView, int viewType) {
            return new SettingsFragment.ViewHolder(convertView);
        }
    }

    /**
     * White, * BlackAndWhite, * Black, * LightBlue <br>
     * <br>
     * // ActionBar text, colorPrimary, colorPrimaryDark (또는 비슷한 색)<br>
     * colorText, colorDrawableCenter, colorDrawableBorder
     */
    private static final int[][] THEME_COLORS_RES = {
            {R.color.material_deep_teal_500_1, android.R.color.white, R.color.primary_material_light_1},
            {R.color.primary_dark_material_dark_1, android.R.color.white, R.color.primary_dark_material_dark_1},
            {R.color.primary_dark_material_dark_1, R.color.primary_dark_material_dark_1, R.color.primary_dark_material_dark_1},
            {R.color.material_light_blue_500, R.color.material_light_blue_500, R.color.material_light_blue_700}
    };

    private static class ViewHolder extends AbsArrayAdapter.SimpleViewHolder {
        final PieProgressDrawable drawable;

        public ViewHolder(View view) {
            super(view);

            Resources resources = view.getResources();
            int drawableSize = resources.getDimensionPixelSize(R.dimen.theme_selector_drawable_size);
            int drawablePadding = resources.getDimensionPixelSize(R.dimen.theme_selector_drawable_padding);

            drawable = new PieProgressDrawable();
            drawable.setBounds(0, 0, drawableSize, drawableSize);
            drawable.setBorderWidth(2, resources.getDisplayMetrics());
            drawable.setLevel(100);

            textView.setCompoundDrawablePadding(drawablePadding);
            textView.setCompoundDrawables(drawable, null, null, null);

        }

        @SuppressWarnings("deprecation")
        void setView(AppTheme appTheme) {
            textView.setText(appTheme.toString());

            int position = appTheme.ordinal();
            Resources res = textView.getResources();

            int colorText = res.getColor(THEME_COLORS_RES[position][0]);
            textView.setTextColor(colorText);

            int colorDrawableCenter = res.getColor(THEME_COLORS_RES[position][1]);
            int colorDrawableBorder = res.getColor(THEME_COLORS_RES[position][2]);

            drawable.setCenterColor(colorDrawableCenter);
            drawable.setColor(colorDrawableBorder);

            textView.invalidateDrawable(drawable);
        }
    }

    /**
     * 어플리케이션의 모든 캐쉬를 삭제한다.
     */
    private void deleteCache() {
        Tasks.newTask(() -> {
            AppUtil.clearCache(getActivity());
            return null;
        }).delayed()
                .result(result -> AppUtil.showToast(getActivity(), R.string.execute_delete))
                .error(e -> AppUtil.showErrorToast(getActivity(), e, true))
                .execute();
    }

    @Override
    public void onResume() {
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.setting);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        }


        if (getFragmentManager().findFragmentByTag("front") != null) {
            getFragmentManager().beginTransaction().hide(this).commit();
        }

        ((SettingActivity) getActivity()).restoreToolbar();

        super.onResume();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (isVisible()) {
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

            if (actionBar != null) {
                actionBar.setTitle(R.string.setting);
                actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
            }

        }

        super.onHiddenChanged(hidden);
    }

    @Override
    public void onPause() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    /**
     * 설정화면 진입시 현재 설정된 값에 따라 설명이 바뀌어야 할 아이템들의 설명을 바꾼다.
     */
    private void bindPreferenceSummaryToValue() {
        SharedPreferences pref = getPreferenceScreen().getSharedPreferences();
        pref.registerOnSharedPreferenceChangeListener(this);

        String[] keys = {
                PrefUtil.KEY_HOME,
                PrefUtil.KEY_CHECK_BORROW,
                PrefUtil.KEY_CHECK_SEAT,
                PrefUtil.KEY_LIB_WIDGET_SEAT_SHOW_ALL,
                PrefUtil.KEY_THEME,
                PrefUtil.KEY_IMAGE_SAVE_PATH, PrefUtil.KEY_DOC_SAVE_PATH,
                //PrefUtil.KEY_BUILDINGS_FETCH_TIME
        };
        for (String key : keys) {
            onSharedPreferenceChanged(pref, key);
        }
    }

    @Override
    public void onDetach() {
        getFragmentManager().removeOnBackStackChangedListener(onBackStackChangedListener);
        super.onDetach();
    }

    @SuppressWarnings("ResourceType")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PrefUtil.KEY_CHECK_BORROW:
                setPrefScreenSummary(sharedPreferences, key, R.string.setting_check_borrow_desc_enable, R.string.setting_check_borrow_desc_disable);
                break;

            case PrefUtil.KEY_CHECK_SEAT:
                setPrefScreenSummary(sharedPreferences, key, R.string.setting_check_seat_desc_enable, R.string.setting_check_seat_desc_disable);
                break;

            case PrefUtil.KEY_LIB_WIDGET_SEAT_SHOW_ALL:
                setPrefScreenSummary(sharedPreferences, key, R.string.setting_widget_seat_show_all_enable, R.string.setting_widget_seat_show_all_disable);
                break;

            case PrefUtil.KEY_IMAGE_SAVE_PATH:
                findPreference(key).setSummary(getString(R.string.setting_save_image_desc, PrefHelper.Data.getPicturePath()));
                break;

            case PrefUtil.KEY_DOC_SAVE_PATH:
                findPreference(key).setSummary(getString(R.string.setting_save_doc_desc, PrefHelper.Data.getDocumentPath()));
                break;

            case PrefUtil.KEY_THEME:
                Preference connectionPref = findPreference(key);
                AppUtil.setTheme(AppTheme.values()[sharedPreferences.getInt(key, 0)]);
                AppUtil.applyTheme(getActivity().getApplicationContext());
                connectionPref.setSummary(getString(R.string.setting_theme_desc, AppUtil.theme().toString()));
                break;

            case PrefUtil.KEY_HOME:
                trackerUtil.sendEvent(TAG, "enable home fragment", Boolean.toString(sharedPreferences.getBoolean(key, true)));

                setPrefScreenSummary(sharedPreferences, key, R.string.setting_home_desc_enable, R.string.setting_home_desc_disable);

                getActivity().setResult(AppUtil.RELAUNCH_ACTIVITY);
                break;

            case PrefUtil.KEY_BUILDINGS_FETCH_TIME:
                connectionPref = findPreference(key);
                long time = PrefHelper.Buildings.downloadTime();

                if (time < 1)
                    connectionPref.setSummary(getString(R.string.setting_building_room_desc, getString(R.string.setting_building_room_fetch_time_none)));
                else {
                    Date date = new Date();
                    date.setTime(time);
                    String dateString = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(date);
                    connectionPref.setSummary(getString(R.string.setting_building_room_desc, dateString));
                }
                break;

            default:
                break;
        }
    }

    private void setPrefScreenSummary(SharedPreferences pref, String key, int enableID, int disableID) {
        boolean value = pref.getBoolean(key, false);
        trackerUtil.sendEvent(TAG, key, String.valueOf(value));
        findPreference(key).setSummary(value ? enableID : disableID);
    }
}
