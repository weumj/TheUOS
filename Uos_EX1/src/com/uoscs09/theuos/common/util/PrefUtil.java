package com.uoscs09.theuos.common.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.preference.PreferenceManager;

public class PrefUtil {
	public static final String KEY_CHECK_ANOUNCE_SERVICE = "ACTIVE_SERVICE_ANOUNCE";
	public static final String KEY_DEL_TIMETABLE = "DELETE_TIMETABLE";
	public static final String KEY_ORDER = "ORDER";
	public static final String KEY_HOME = "SCREEN_HOME";
	public static final String KEY_KEYWORD_ANOUNCE = "KEYWORD_ANOUNCE";
	public static final String KEY_CHECK_BORROW = "BORROW";
	public static final String KEY_CHECK_SEAT = "LIB_SEAT";
	public static final String KEY_NOTI_TIME = "NOTI_TIME";
	public static final String KEY_SAVE_ROUTE = "IMAGE_SAVE_ROUTE";
	public static final String KEY_THEME = "APP_THEME";
	public static final String KEY_TIMETABLE_NOTIFY = "TIMETABLE_NOTIFY";
	public static final String KEY_TIMETABLE_NOTIFY_TIME = "TIMETABLE_NOTIFY_TIME";
	public static final String KEY_TIMETABLE_LIMIT = "TIMETABLE_LIMIT";
	public static final String KEY_REST_DATE_TIME = "REST_DATE_TIME";
	public static final int TIMETABLE_LIMIT_MIN = 9;
	public static final int TIMETABLE_LIMIT_MAX = 15;
	private static PrefUtil instance;
	private SharedPreferences pref;
	private Editor edit;

	public static synchronized PrefUtil getInstance(Context context) {
		if (instance == null)
			instance = new PrefUtil(context);
		return instance;
	}

	@SuppressLint("CommitPrefEdits")
	private PrefUtil(Context context) {
		this.pref = PreferenceManager.getDefaultSharedPreferences(context
				.getApplicationContext());
		this.edit = pref.edit();
	}

	@Override
	protected void finalize() throws Throwable {
		instance = null;
		super.finalize();
	}

	public synchronized void put(String key, boolean value) {
		edit.putBoolean(key, value);
		edit.commit();
	}

	public synchronized void put(String key, int value) {
		edit.putInt(key, value);
		edit.commit();
	}

	public synchronized void put(String key, String value) {
		edit.putString(key, value);
		edit.commit();
	}

	public synchronized void put(String key, float value) {
		edit.putFloat(key, value);
		edit.commit();
	}

	public synchronized void put(String key, long value) {
		edit.putLong(key, value);
		edit.commit();
	}

	public boolean get(String key, boolean defValue) {
		return pref.getBoolean(key, defValue);
	}

	public int get(String key, int defValue) {
		return pref.getInt(key, defValue);
	}

	public String get(String key, String defValue) {
		return pref.getString(key, defValue);
	}

	public float get(String key, float defValue) {
		return pref.getFloat(key, defValue);
	}

	public long get(String key, long defValue) {
		return pref.getLong(key, defValue);
	}

	public static String getSaveRoute(Context context) {
		return PrefUtil.getInstance(context).get(
				KEY_SAVE_ROUTE,
				Environment.getExternalStoragePublicDirectory(
						Environment.DIRECTORY_PICTURES).toString()
						+ "/");
	}
}
