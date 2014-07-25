package com.uoscs09.theuos.setting;

import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pkg.asyncexcute.AsyncCallback;
import pkg.asyncexcute.AsyncExecutor;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.AppUtil.AppTheme;
import com.uoscs09.theuos.common.util.PrefUtil;
import com.uoscs09.theuos.http.HttpRequest;

/** 메인 설정화면을 나타내는 {@code PreferenceFragment} */
public class SettingsFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {
	private AlertDialog themeSelectorDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		addPreferencesFromResource(R.xml.prefrence);
		bindPreferenceSummaryToValue();
	}

	private void changeFragment(Class<? extends Fragment> clazz) {
		getFragmentManager()
				.beginTransaction()
				.hide(this)
				.add(android.R.id.content,
						Fragment.instantiate(getActivity(), clazz.getName()),
						"front").addToBackStack(null).commit();
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		switch (preference.getTitleRes()) {
		case R.string.setting_order:
			changeFragment(SettingsOrderFragment.class);
			return true;
		case R.string.setting_theme:
			showThemeDialog();
			return true;
		case R.string.setting_anounce_noti:
			changeFragment(SettingsAnounceNotiFragment.class);
			return true;
		case R.string.setting_delete_cache:
			deleteCache(getActivity());
			return true;
		case R.string.setting_timetable:
			changeFragment(SettingsTimetableFragment.class);
			return true;
		case R.string.setting_save_route:
			// changeFragment(SettingsFileSelectDialogFragment.class);
			new SettingsFileSelectDialogFragment().show(getFragmentManager(),
					null);
			return true;
		case R.string.setting_web_page:
			changeFragment(SettingsWebPageFragment.class);
			return true;
		case R.string.setting_app_version_update:
			// showAppVersionDialog();
			return true;
		default:
			return false;
		}
	}

	private void showAppVersionDialog() {
		final Context context = getActivity();
		// final String URL =
		// "https://play.google.com/store/apps/details?id=com.uoscs09.theuos";
		final String URL = "https://play.google.com/store/apps/details?id=com.smartlotte";
		final ProgressDialog progress = AppUtil.getProgressDialog(context,
				false, getText(R.string.progress_while_updating), null);
		progress.show();
		AsyncExecutor<String> executor = new AsyncExecutor<String>();
		executor.setCallable(new Callable<String>() {

			@Override
			public String call() throws Exception {
				String body = HttpRequest.getBody(URL);
				Pattern p = Pattern.compile("현재버전\\n\n.[0-9]\\..[0-9]");
				Matcher m = p.matcher(body);
				return m.group();
			}
		}).setCallback(new AsyncCallback.Base<String>() {
			@Override
			public void onResult(String result) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				TextView tv = new TextView(context);
				String thisVersion = getString(R.string.setting_app_version_desc);
				if (thisVersion.equals(result)) {
					tv.setText(R.string.setting_app_version_update_this_new);
					builder.setView(tv).setPositiveButton(android.R.string.ok,
							null);
				} else {
					tv.setText(R.string.setting_app_version_update_this_new
							+ result);
					builder.setView(tv)
							.setPositiveButton("업데이트",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											startActivity(AppUtil
													.setWebPageIntent(URL));
										}
									})
							.setNegativeButton(android.R.string.cancel, null);
				}
				builder.create().show();
			}

			@Override
			public void exceptionOccured(Exception e) {
				AppUtil.showErrorToast(context, e, true);
			}

			@Override
			public void onPostExcute() {
				progress.dismiss();
			}
		}).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	/** 테마를 선택하는 dialog를 보여준다.dialog가 null일시 초기화도 같이한다. */
	private void showThemeDialog() {
		if (themeSelectorDialog == null) {
			AppTheme[] values = AppTheme.values();
			int size = values.length;
			String[] items = new String[size];
			int i = 0;
			for (AppTheme at : values) {
				items[i++] = at.toString();
			}
			themeSelectorDialog = new AlertDialog.Builder(getActivity())
					.setTitle(R.string.setting_plz_select_theme)
					.setItems(items, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							PrefUtil pref = PrefUtil.getInstance(getActivity());
							switch (which) {
							case 1:
							case 2:
								pref.put(PrefUtil.KEY_THEME, which);
								break;
							default:
								pref.put(PrefUtil.KEY_THEME, 0);
								break;
							}
							onSharedPreferenceChanged(getPreferenceScreen()
									.getSharedPreferences(), PrefUtil.KEY_THEME);
							themeSelectorDialog.dismiss();
							getActivity().setResult(AppUtil.RELAUNCH_ACTIVITY);
						}
					}).create();
		}
		themeSelectorDialog.show();
	}

	/** 어플리케이션의 모든 캐쉬를 삭제한다. */
	private void deleteCache(Context context) {
		try {
			AppUtil.clearApplicationFile(context.getCacheDir());
			AppUtil.clearApplicationFile(context.getExternalCacheDir());
			AppUtil.showToast(context, R.string.excute_delete,
					getUserVisibleHint());
		} catch (Exception e) {
			AppUtil.showErrorToast(context, e, true);
		}
	}

	@Override
	public void onResume() {
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setTitle(R.string.setting);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP
				| ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);

		super.onResume();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		if (isVisible()) {
			getPreferenceScreen().getSharedPreferences()
					.registerOnSharedPreferenceChangeListener(this);
			ActionBar actionBar = getActivity().getActionBar();
			actionBar.setTitle(R.string.setting);
			actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP
					| ActionBar.DISPLAY_SHOW_HOME
					| ActionBar.DISPLAY_SHOW_TITLE);
		}
		super.onHiddenChanged(hidden);
	}

	@Override
	public void onPause() {
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	/** 설정화면 진입시 현재 설정된 값에 따라 설명이 바뀌어야 할 아이템들의 설명을 바꾼다. */
	private void bindPreferenceSummaryToValue() {
		SharedPreferences pref = getPreferenceScreen().getSharedPreferences();
		pref.registerOnSharedPreferenceChangeListener(this);
		String[] keys = { PrefUtil.KEY_CHECK_BORROW, PrefUtil.KEY_CHECK_SEAT,
				PrefUtil.KEY_SAVE_ROUTE, PrefUtil.KEY_THEME };
		for (String key : keys) {
			onSharedPreferenceChanged(pref, key);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(PrefUtil.KEY_CHECK_BORROW)) {
			setPrefScreenSummary(sharedPreferences, key,
					R.string.setting_check_borrow_desc_enable,
					R.string.setting_check_borrow_desc_disable);
		} else if (key.equals(PrefUtil.KEY_CHECK_SEAT)) {
			setPrefScreenSummary(sharedPreferences, key,
					R.string.setting_check_seat_desc_enable,
					R.string.setting_check_seat_desc_disable);
		} else if (key.equals(PrefUtil.KEY_SAVE_ROUTE)) {
			findPreference(key)
					.setSummary(PrefUtil.getSaveRoute(getActivity()));
		} else if (key.equals(PrefUtil.KEY_THEME)) {
			Preference connectionPref = findPreference(key);
			Activity activity = getActivity();
			AppUtil.theme = AppTheme.values()[sharedPreferences.getInt(key, 0)];
			AppUtil.applyTheme(activity.getApplicationContext());
			connectionPref.setSummary(getString(R.string.setting_theme_desc)
					+ "\n현재 적용된 테마 : " + AppUtil.theme.toString());
		} else if (key.equals(PrefUtil.KEY_HOME)) {
			getActivity().setResult(AppUtil.RELAUNCH_ACTIVITY);
		}
	}

	private void setPrefScreenSummary(SharedPreferences pref, String key,
			int enableID, int disableID) {
		Preference connectionPref = findPreference(key);
		if (pref.getBoolean(key, false)) {
			connectionPref.setSummary(enableID);
		} else {
			connectionPref.setSummary(disableID);
		}
	}
}
