package com.uoscs09.theuos2.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.uoscs09.theuos2.R;

import java.util.Map;
import java.util.Set;

//FIXME 모든 설정값에 default preference를 쓰지 말고, 설정에 따라 분리할 필요가 있음
public class PrefUtil {
    /**
     * 공지사항 알리미를 사용 할 것인지 여부, {@code boolean}
     *
     * @see R.string#pref_key_check_anounce_service
     */
    public static final String KEY_CHECK_ANOUNCE_SERVICE = "ACTIVE_SERVICE_ANOUNCE";
    // public static final String KEY_ORDER = "ORDER";
    /**
     * 홈 화면을 보여줄 것인지 여부, {@code boolean}
     *
     * @see R.string#pref_key_home
     */
    public static final String KEY_HOME = "SCREEN_HOME";
    /**
     * 공지사항 알리미의 키워드, {@code String}
     *
     * @see R.string#pref_key_keyword_anounce
     */
    public static final String KEY_KEYWORD_ANOUNCE = "KEYWORD_ANOUNCE";
    /**
     * 대출 불가능한 도서 표시 여부, {@code boolean}
     *
     * @see R.string#pref_key_check_borrow
     */
    public static final String KEY_CHECK_BORROW = "BORROW";
    /**
     * 점유된 스터디룸 표시 여부, {@code boolean}
     *
     * @see R.string#pref_key_check_seat
     */
    public static final String KEY_CHECK_SEAT = "LIB_SEAT";
    /**
     * 도서관 좌석 위젯 스터디룸만 표시 여부, {@code boolean}
     *
     * @see R.string#pref_key_widget_seat_show_all
     */
    public static final String KEY_LIB_WIDGET_SEAT_SHOW_ALL = "LIB_WIDGET_SEAT_SHOW_ALL";
    /**
     * 공지사항 알리미의 알림 시간, {@code String}
     *
     * @see R.string#pref_key_noti_time
     */
    public static final String KEY_NOTI_TIME = "NOTI_TIME";
    /**
     * 이미지가 저장되는 경로, {@code String}
     *
     * @see R.string#pref_key_save_image_route
     */
    public static final String KEY_IMAGE_SAVE_PATH = "IMAGE_SAVE_ROUTE";

    /**
     * 텍스트가 저장되는 경로, {@code String}
     *
     * @see R.string#pref_key_save_txt_route
     */
    public static final String KEY_TXT_SAVE_PATH = "TXT_SAVE_ROUTE";
    /**
     * 어플리케이션의 테마, {@code int}
     *
     * @see R.string#pref_key_theme
     */
    public static final String KEY_THEME = "APP_THEME";
    /**
     * 시간표 알리미의 사용 여부, {@code boolean}
     */
    public static final String KEY_CHECK_TIMETABLE_NOTIFY_SERVICE = "ACTIVE_TIMETABLE_NOTIFY_SERVICE";

    /**
     * 시간표 표시 제한 여부, {@code boolean}
     *
     * @see R.string#pref_key_timetable_limit
     */
    public static final String KEY_TIMETABLE_LIMIT = "TIMETABLE_LIMIT_LAST_TIME";
    /**
     * 식단표를 웹에서 받아온 시각, {@code int}
     */
    public static final String KEY_REST_DATE_TIME = "REST_DATE_TIME";
    private static PrefUtil instance;
    private SharedPreferences pref;

    public static synchronized PrefUtil getInstance(Context context) {
        if (instance == null)
            instance = new PrefUtil(context);
        return instance;
    }

    private PrefUtil(Context context) {
        this.pref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    @Override
    protected void finalize() throws Throwable {
        instance = null;
        super.finalize();
    }

    public boolean put(String key, boolean value) {
        return pref.edit().putBoolean(key, value).commit();
    }

    public boolean put(String key, int value) {
        return pref.edit().putInt(key, value).commit();
    }

    public boolean put(String key, String value) {
        return pref.edit().putString(key, value).commit();
    }

    public boolean put(String key, float value) {
        return pref.edit().putFloat(key, value).commit();
    }

    public boolean put(String key, long value) {
        return pref.edit().putLong(key, value).commit();
    }

    public boolean put(String key, Set<String> values) {
        return pref.edit().putStringSet(key, values).commit();
    }

    /**
     * @throws ClassCastException 기존에 저장된 값의 Type이 {@code boolean}이 아닐 때
     */
    public boolean get(String key, boolean defValue) {
        return pref.getBoolean(key, defValue);
    }

    /**
     * @throws ClassCastException 기존에 저장된 값의 Type이 {@code int}가 아닐 때
     */
    public int get(String key, int defValue) {
        return pref.getInt(key, defValue);
    }

    /**
     * @throws ClassCastException 기존에 저장된 값의 Type이 {@code String}이 아닐 때
     */
    public String get(String key, String defValue) {
        return pref.getString(key, defValue);
    }

    /**
     * @throws ClassCastException 기존에 저장된 값의 Type이 {@code float}이 아닐 때
     */
    public float get(String key, float defValue) {
        return pref.getFloat(key, defValue);
    }

    /**
     * @throws ClassCastException 기존에 저장된 값의 Type이 {@code long}이 아닐 때
     */
    public long get(String key, long defValue) {
        return pref.getLong(key, defValue);
    }

    /**
     * @throws ClassCastException 기존에 저장된 값의 Type이 {@code Set<String>}이 아닐 때
     */
    public Set<String> get(String key, Set<String> defValues) {
        return pref.getStringSet(key, defValues);
    }

    public void remove(String key) {
        pref.edit().remove(key).commit();
    }

    public void remove(String... keys) {

        if (keys != null) {
            SharedPreferences.Editor editor = pref.edit();
            for (String key : keys) editor.remove(key);
            editor.commit();
        }

    }

    public Map<String, ?> getAll() {
        return pref.getAll();
    }

    /**
     * 그림파일이 저장되는 경로를 얻는다.
     */
    public static String getPicturePath(Context context) {
        return getInstance(context).get(KEY_IMAGE_SAVE_PATH, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/");
    }

    public static String getDocumentPath(Context context) {
        return getInstance(context).get(KEY_TXT_SAVE_PATH, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/");
    }

    public static String getDefaultPath(final String key){
        switch (key){
            default:
            case KEY_IMAGE_SAVE_PATH:
                return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            case KEY_TXT_SAVE_PATH:
                return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        }
    }
}
