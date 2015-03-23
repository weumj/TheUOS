package com.uoscs09.theuos2.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.javacan.asyncexcute.AsyncCallback;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.common.AsyncLoader;
import com.uoscs09.theuos2.common.PieProgressDrawable;
import com.uoscs09.theuos2.common.UOSApplication;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.AppUtil.AppTheme;
import com.uoscs09.theuos2.util.PrefUtil;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import java.util.Formatter;
import java.util.concurrent.Callable;

/**
 * 메인 설정화면을 나타내는 {@code PreferenceFragment}
 */
public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    private AlertDialog mThemeSelectorDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fragment가 재생성 되었을 때, 표시 화면을 정확히 복구하기 위한 설정
        getFragmentManager().addOnBackStackChangedListener(new OnBackStackChangedListener() {

            @Override
            public void onBackStackChanged() {
                if (getFragmentManager() != null) {
                    if (getFragmentManager().getBackStackEntryCount() < 1) {
                        getFragmentManager().beginTransaction()
                                .show(SettingsFragment.this)
                                .commit();
                    }
                }
            }
        });

        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.prefrence);
        bindPreferenceSummaryToValue();
    }

    private void changeFragment(Class<? extends Fragment> clazz) {
        getFragmentManager().beginTransaction()
                .hide(this)
                .add(android.R.id.tabcontent, Fragment.instantiate(getActivity(), clazz.getName()), "front")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, @NonNull Preference preference) {
        switch (preference.getTitleRes()) {
            case R.string.setting_order:
                changeFragment(SettingsOrderFragment.class);
                return true;

            case R.string.setting_theme:
                showThemeDialog();
                return true;

            case R.string.setting_announce_noti:
                changeFragment(SettingsAnnounceNotificationFragment.class);
                return true;

            case R.string.setting_delete_cache:
                deleteCache();
                return true;

            case R.string.setting_timetable:
                changeFragment(SettingsTimetableFragment.class);
                return true;

            case R.string.setting_save_route_sub_title:
                new SettingsFileSelectDialogFragment().show(getFragmentManager(), null);
                return true;

            case R.string.setting_web_page:
                changeFragment(SettingsWebPageFragment.class);
                return true;

            case R.string.setting_app_version_update:
                showAppVersionDialog();
                return true;

            default:
                return false;
        }
    }

    private void showAppVersionDialog() {
        final String URL = "https://play.google.com/store/apps/details?id=com.uoscs09.theuos2";
        final Dialog progress = AppUtil.getProgressDialog(getActivity(), false, getText(R.string.progress_while_updating), null);
        progress.show();

        AsyncLoader.excute(new Callable<String>() {

            @Override
            public String call() throws Exception {
                String body = HttpRequest.getBody(URL);
                Source s = new Source(body);
                Element e = s.getAllElementsByClass("details-section metadata").get(0)
                        .getAllElementsByClass("details-section-contents").get(0).getAllElementsByClass("meta-info").get(3)
                        .getAllElementsByClass("content").get(0);
                return e.getTextExtractor().toString().trim();

            }
        }, new AsyncCallback.Base<String>() {
            @Override
            public void onResult(String result) {
                String thisVersion = getString(R.string.setting_app_version_desc);
                if (thisVersion.equals(result)) {
                    AppUtil.showToast(getActivity(), R.string.setting_app_version_update_this_new, true);
                } else {

                    TextView tv = new TextView(getActivity());
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    tv.setPadding(100, 20, 20, 20);

                    Formatter f = new Formatter();
                    f.format(getString(R.string.setting_app_version_update_this_old), thisVersion);
                    tv.setText(f.toString() + " " + result);
                    f.close();

                    AlertDialog d = new MaterialDialog.Builder(getActivity())
                            .title(R.string.setting_app_version_update_require)
                            .iconAttr(R.attr.ic_action_about)
                            .positiveText(R.string.update)
                            .negativeText(R.string.later)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    startActivity(AppUtil.setWebPageIntent(URL));
                                }
                            })
                            .build();
                    d.show();
                }
            }

            @Override
            public void exceptionOccured(Exception e) {
                AppUtil.showErrorToast(getActivity(), e, true);
            }

            @Override
            public void onPostExcute() {
                progress.dismiss();
            }
        });
    }

    /**
     * 테마를 선택하는 dialog를 보여준다.dialog가 null일시 초기화도 같이한다.
     */
    private void showThemeDialog() {
        if (mThemeSelectorDialog == null) {
            mThemeSelectorDialog = new MaterialDialog.Builder(getActivity())
                    .iconAttr(R.attr.ic_content_paint)
                    .title(R.string.setting_plz_select_theme)
                    .adapter(new ThemeSelectAdapter(getActivity()),
                            new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                                    PrefUtil pref = PrefUtil.getInstance(getActivity());
                                    int originalValue = pref.get(PrefUtil.KEY_THEME, 0);

                                    if (originalValue != i) {

                                        Tracker t = ((UOSApplication) getActivity().getApplication()).getTracker(UOSApplication.TrackerName.APP_TRACKER);
                                        t.send(new HitBuilders.EventBuilder()
                                                .setCategory("setting fragment")
                                                .setAction("apply theme")
                                                .setLabel(AppTheme.values()[i].name())
                                                .setValue(i)
                                                .build());

                                        pref.put(PrefUtil.KEY_THEME, i);
                                        onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences(), PrefUtil.KEY_THEME);
                                        getActivity().setResult(AppUtil.RELAUNCH_ACTIVITY);
                                    }

                                    mThemeSelectorDialog.dismiss();
                                }
                            })
                    .build();
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
        public SettingsFragment.ViewHolder getViewHolder(View convertView) {
            return new SettingsFragment.ViewHolder(convertView);
        }
    }

    /**
     * White, * BlackAndWhite, * Black, * LightBlue <br>
     * <br>
     * // ActionBar text, colorPrimary, colorPrimaryDark (또는 비슷한 색)<br>
     * colorText, colorDrawableCentor, colorDrawableBorder
     */
    static final int[][] THEME_COLORS_RES = {
            {R.color.material_deep_teal_500, android.R.color.white, R.color.primary_material_light},
            {R.color.primary_dark_material_dark, android.R.color.white, R.color.primary_dark_material_dark},
            {R.color.primary_dark_material_dark, R.color.primary_material_dark, R.color.primary_dark_material_dark},
            {R.color.material_light_blue_400, R.color.material_light_blue_400, R.color.material_light_blue_600}
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

        void setView(AppTheme appTheme) {
            textView.setText(appTheme.toString());

            int position = appTheme.ordinal();
            Resources res = textView.getResources();

            int colorText = res.getColor(THEME_COLORS_RES[position][0]);
            textView.setTextColor(colorText);

            int colorDrawableCentor = res.getColor(THEME_COLORS_RES[position][1]);
            int colorDrawableBorder = res.getColor(THEME_COLORS_RES[position][2]);

            drawable.setCentorColor(colorDrawableCentor);
            drawable.setColor(colorDrawableBorder);

            textView.invalidateDrawable(drawable);
        }
    }

    /**
     * 어플리케이션의 모든 캐쉬를 삭제한다.
     */
    private void deleteCache() {
        AsyncLoader.excute(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                AppUtil.clearCache(getActivity());
                return null;
            }
        }, new AsyncLoader.OnTaskFinishedListener<Void>() {
            @Override
            public void onTaskFinished(boolean isExceptionOccurred, Void data, Exception e) {
                if (isExceptionOccurred) {
                    AppUtil.showErrorToast(getActivity(), e, true);
                } else {
                    AppUtil.showToast(getActivity(), R.string.execute_delete);
                }
            }
        });
    }

    @Override
    public void onResume() {
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(R.string.setting);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);


        if (getFragmentManager().findFragmentByTag("front") != null) {
            getFragmentManager().beginTransaction().hide(this).commit();
        }

        super.onResume();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (isVisible()) {
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();

            actionBar.setTitle(R.string.setting);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
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

        String[] keys = {PrefUtil.KEY_CHECK_BORROW, PrefUtil.KEY_CHECK_SEAT, PrefUtil.KEY_LIB_WIDGET_SEAT_SHOW_ALL, PrefUtil.KEY_IMAGE_SAVE_PATH, PrefUtil.KEY_THEME};
        for (String key : keys) {
            onSharedPreferenceChanged(pref, key);
        }
    }

    @Override
    public void onDetach() {
        getFragmentManager().addOnBackStackChangedListener(null);
        super.onDetach();
    }

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
                findPreference(key).setSummary(PrefUtil.getPictureSavedPath(getActivity()));
                break;

            case PrefUtil.KEY_THEME:
                Preference connectionPref = findPreference(key);
                Activity activity = getActivity();
                AppUtil.theme = AppTheme.values()[sharedPreferences.getInt(key, 0)];
                AppUtil.applyTheme(activity.getApplicationContext());
                connectionPref.setSummary(getString(R.string.setting_theme_desc) + "\n현재 적용된 테마 : " + AppUtil.theme.toString());
                break;

            case PrefUtil.KEY_HOME:
                Tracker t = ((UOSApplication) getActivity().getApplication()).getTracker(UOSApplication.TrackerName.APP_TRACKER);
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("setting fragment")
                        .setAction("enable home fragment")
                        .setLabel("" + sharedPreferences.getBoolean(key, true))
                        .build());

                getActivity().setResult(AppUtil.RELAUNCH_ACTIVITY);
                break;
        }
    }

    private void setPrefScreenSummary(SharedPreferences pref, String key, int enableID, int disableID) {

        findPreference(key).setSummary(pref.getBoolean(key, false) ? enableID : disableID);

    }
}
