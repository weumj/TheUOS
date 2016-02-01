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
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.async.AsyncUtil;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.common.PieProgressDrawable;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.parse.JerichoParser;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.AppUtil.AppTheme;
import com.uoscs09.theuos2.util.PrefUtil;
import com.uoscs09.theuos2.util.TrackerUtil;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

/**
 * 메인 설정화면을 나타내는 {@code PreferenceFragment}
 */
public class SettingsFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {
    private AlertDialog mThemeSelectorDialog;

    private static final String TAG = "SettingsFragment";
    private static final String APP_URL = "https://play.google.com/store/apps/details?id=com.uoscs09.theuos2";

    private FragmentManager.OnBackStackChangedListener onBackStackChangedListener = () -> {
        if (getFragmentManager() != null) {
            if (getFragmentManager().getBackStackEntryCount() < 1) {
                getFragmentManager().beginTransaction()
                        .show(SettingsFragment.this)
                        .commit();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fragment가 재생성 되었을 때, 표시 화면을 정확히 복구하기 위한 설정
        getFragmentManager().addOnBackStackChangedListener(onBackStackChangedListener);

        setHasOptionsMenu(true);

        TrackerUtil.getInstance(this).sendVisibleEvent(TAG);
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

        if (title.equals(getText(R.string.setting_order))) {
            changeFragment(SettingsOrderFragment.class);
            return true;
        } else if (title.equals(getText(R.string.setting_theme))) {
            showThemeDialog();
            return true;
        } else if (title.equals(getText(R.string.setting_announce_except_type_notice))) {
            TrackerUtil.getInstance(this).sendEvent(TAG, "announce_except_type_notice");
            return true;
        } else if (title.equals(getText(R.string.setting_delete_cache))) {
            TrackerUtil.getInstance(this).sendEvent(TAG, "delete cache");
            deleteCache();
            return true;

        } else if (title.equals(getText(R.string.setting_timetable))) {
            changeFragment(SettingsTimetableFragment.class);
            return true;

        } else if (title.equals(getText(R.string.setting_save_text_sub_title))) {
            TrackerUtil.getInstance(this).sendEvent(TAG, "change directory - " + "text");
            SettingsFileSelectDialogFragment f = new SettingsFileSelectDialogFragment();
            f.setSavePathKey(preference.getKey());
            f.setTitle(title);
            f.show(getFragmentManager(), null);
            return true;
        } else if (title.equals(getText(R.string.setting_save_image_sub_title))) {
            TrackerUtil.getInstance(this).sendEvent(TAG, "change directory - " + "image");
            SettingsFileSelectDialogFragment f = new SettingsFileSelectDialogFragment();
            f.setSavePathKey(preference.getKey());
            f.setTitle(title);
            f.show(getFragmentManager(), null);
            return true;
        } else if (title.equals(getText(R.string.setting_web_page))) {
            changeFragment(SettingsWebPageFragment.class);
            return true;
        } else if (title.equals(getText(R.string.setting_app_version_update))) {
            TrackerUtil.getInstance(this).sendEvent(TAG, "check app version");
            showAppVersionDialog();
            return true;
        }
        return false;
    }

    private void showAppVersionDialog() {

        final Dialog progress = AppUtil.getProgressDialog(getActivity(), false, getText(R.string.progress_version_check), null);
        progress.show();

        HttpRequest.Builder.newStringRequestBuilder(APP_URL)
                .build()
                .checkNetworkState(getActivity())
                .wrap(new JerichoParser<String>() {
                    @Override
                    protected String parseHtmlBody(Source source) throws Exception {
                        Element e = source.getAllElementsByClass("details-section metadata").get(0)
                                .getAllElementsByClass("details-section-contents").get(0).getAllElementsByClass("meta-info").get(3)
                                .getAllElementsByClass("content").get(0);
                        return e.getTextExtractor().toString().trim();
                    }
                })
                .getAsync(result -> {
                            progress.dismiss();
                            String thisVersion = getString(R.string.setting_app_version_name);
                            if (thisVersion.equals(result)) {
                                AppUtil.showToast(getActivity(), R.string.setting_app_version_update_this_new, true);
                            } else {

                                TextView tv = new TextView(getActivity());
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                                tv.setPadding(100, 20, 20, 20);

                                tv.setText(getString(R.string.setting_app_version_update_this_old, thisVersion, result));

                                AlertDialog d = new AlertDialog.Builder(getActivity())
                                        .setView(tv)
                                        .setTitle(R.string.setting_app_version_update_exist)
                                        .setIcon(R.drawable.theme_ic_action_action_about)
                                        .setPositiveButton(R.string.update, (dialog, which) -> {
                                            startActivity(AppUtil.getWebPageIntent(APP_URL));
                                        })
                                        .setNegativeButton(R.string.later, null)
                                        .create();
                                d.show();
                            }
                        },
                        e -> {
                            progress.dismiss();
                            AppUtil.showErrorToast(getActivity(), e, true);
                        }
                );
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
                        PrefUtil pref = PrefUtil.getInstance(getActivity());
                        int originalValue = pref.get(PrefUtil.KEY_THEME, 0);

                        if (originalValue != i) {

                            TrackerUtil.getInstance(SettingsFragment.this).sendEvent(TAG, "apply theme", AppTheme.values()[i].name(), i);

                            pref.put(PrefUtil.KEY_THEME, i);
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
        public ThemeSelectAdapter(Context context) {
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
        AsyncUtil.newRequest(
                () -> {
                    AppUtil.clearCache(getActivity());
                    return null;
                })
                .getAsync(
                        result -> AppUtil.showToast(getActivity(), R.string.execute_delete),
                        e -> AppUtil.showErrorToast(getActivity(), e, true)
                );

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
                PrefUtil.KEY_ANNOUNCE_EXCEPT_TYPE_NOTICE,
                PrefUtil.KEY_CHECK_BORROW,
                PrefUtil.KEY_CHECK_SEAT,
                PrefUtil.KEY_LIB_WIDGET_SEAT_SHOW_ALL,
                PrefUtil.KEY_THEME,
                PrefUtil.KEY_IMAGE_SAVE_PATH, PrefUtil.KEY_TXT_SAVE_PATH
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
                findPreference(key).setSummary(PrefUtil.getPicturePath(getActivity()));
                break;

            case PrefUtil.KEY_TXT_SAVE_PATH:
                findPreference(key).setSummary(PrefUtil.getDocumentPath(getActivity()));
                break;

            case PrefUtil.KEY_THEME:
                Preference connectionPref = findPreference(key);
                AppUtil.theme = AppTheme.values()[sharedPreferences.getInt(key, 0)];
                AppUtil.applyTheme(getActivity().getApplicationContext());
                connectionPref.setSummary(getString(R.string.setting_theme_desc) + "\n현재 적용된 테마 : " + AppUtil.theme.toString());
                break;

            case PrefUtil.KEY_HOME:
                TrackerUtil.getInstance(this).sendEvent(TAG, "enable home fragment", "" + sharedPreferences.getBoolean(key, true));

                getActivity().setResult(AppUtil.RELAUNCH_ACTIVITY);
                break;

            default:
                break;
        }
    }

    private void setPrefScreenSummary(SharedPreferences pref, String key, int enableID, int disableID) {
        boolean value = pref.getBoolean(key, false);
        TrackerUtil.getInstance(this).sendEvent(TAG, key, String.valueOf(value));
        findPreference(key).setSummary(value ? enableID : disableID);
    }
}
