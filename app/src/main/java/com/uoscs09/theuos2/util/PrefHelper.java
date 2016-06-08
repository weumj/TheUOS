package com.uoscs09.theuos2.util;

import android.Manifest;
import android.os.Environment;
import android.support.annotation.RequiresPermission;

import com.uoscs09.theuos2.tab.restaurant.WeekRestItem;

import java.util.Calendar;

import static com.uoscs09.theuos2.util.PrefUtil.KEY_ANNOUNCE_EXCEPT_TYPE_NOTICE;
import static com.uoscs09.theuos2.util.PrefUtil.KEY_BUILDINGS_FETCH_TIME;
import static com.uoscs09.theuos2.util.PrefUtil.KEY_CHECK_BORROW;
import static com.uoscs09.theuos2.util.PrefUtil.KEY_CHECK_SEAT;
import static com.uoscs09.theuos2.util.PrefUtil.KEY_CHECK_TIMETABLE_NOTIFY_SERVICE;
import static com.uoscs09.theuos2.util.PrefUtil.KEY_HOME;
import static com.uoscs09.theuos2.util.PrefUtil.KEY_IMAGE_SAVE_PATH;
import static com.uoscs09.theuos2.util.PrefUtil.KEY_LIB_WIDGET_SEAT_SHOW_ALL;
import static com.uoscs09.theuos2.util.PrefUtil.KEY_REST_DATE_TIME;
import static com.uoscs09.theuos2.util.PrefUtil.KEY_REST_WEEK_FETCH_TIME;
import static com.uoscs09.theuos2.util.PrefUtil.KEY_SCHEDULE_FETCH_MONTH;
import static com.uoscs09.theuos2.util.PrefUtil.KEY_THEME;
import static com.uoscs09.theuos2.util.PrefUtil.KEY_TIMETABLE_LIMIT;
import static com.uoscs09.theuos2.util.PrefUtil.KEY_TXT_SAVE_PATH;
import static com.uoscs09.theuos2.util.PrefUtil.getInstance;

public class PrefHelper {

    private static PrefUtil pref() {
        return getInstance(AppUtil.context());
    }

    public static class Screens {
        public static boolean isHomeEnable() {
            return pref().get(KEY_HOME, true);
        }

        public static AppUtil.AppTheme getAppTheme() {
            return AppUtil.AppTheme.values()[pref().get(KEY_THEME, 0)];
        }

        public static void putAppTheme(int i) {
            pref().put(KEY_THEME, i);
        }
    }

    public static class Announces {
        public static boolean isAnnounceExceptNoticeType() {
            return pref().get(KEY_ANNOUNCE_EXCEPT_TYPE_NOTICE, false);
        }
    }

    public static class Books {
        public static boolean isFilterUnavailableBook() {
            return pref().get(KEY_CHECK_BORROW, false);
        }
    }

    public static class LibrarySeats {
        public static boolean isFilterOccupyingRoom() {
            return pref().get(KEY_CHECK_SEAT, false);
        }

        public static boolean isShowingWidgetStudyRoom() {
            return pref().get(KEY_LIB_WIDGET_SEAT_SHOW_ALL, false);
        }
    }

    public static class TimeTables {
        public static boolean isNotifyServiceEnable() {
            return pref().get(KEY_CHECK_TIMETABLE_NOTIFY_SERVICE, false);
        }

        public static void putNotifyServiceEnable(boolean enable) {
            pref().put(KEY_CHECK_TIMETABLE_NOTIFY_SERVICE, enable);
        }

        public static boolean isShowingLastEmptyPeriod() {
            return pref().get(KEY_TIMETABLE_LIMIT, false);
        }
    }

    public static class Restaurants {
        public static boolean isDownloadTimeWithin(int time) {
            return OApiUtil.getDateTime() - PrefHelper.Restaurants.getDownloadTime() < time;
        }

        public static int getDownloadTime() {
            return pref().get(KEY_REST_DATE_TIME, 0);
        }

        public static void putDownloadTime(int time) {
            pref().put(KEY_REST_DATE_TIME, time);
        }

        public static boolean isTodayWithinWeekItemFetchTime(String code, int today) {
            int start = pref().get(KEY_REST_WEEK_FETCH_TIME + "_START_" + code, today + 1);
            int end = pref().get(KEY_REST_WEEK_FETCH_TIME + "_END_" + code, today - 1);

            return (start <= today) && (today <= end);
        }

        /*
        public static int[] getWeekItemFetchTime(String code, int today) {
            return new int[]{
                    pref().get(KEY_REST_WEEK_FETCH_TIME + "_START_" + code, today + 1),
                    pref().get(KEY_REST_WEEK_FETCH_TIME + "_END_" + code, today - 1)
            };
        }
        */

        public static void putWeekItemFetchTime(String code, WeekRestItem item) {
            pref().put(PrefUtil.KEY_REST_WEEK_FETCH_TIME + "_START_" + code, item.startDate);
            pref().put(PrefUtil.KEY_REST_WEEK_FETCH_TIME + "_END_" + code, item.endDate);
        }
    }

    public static class UnivSchedules {
        public static boolean isMonthEqualToFetchMonth() {
            return getFetchMonth() == Calendar.getInstance().get(Calendar.MONTH);
        }

        public static int getFetchMonth() {
            return pref().get(KEY_SCHEDULE_FETCH_MONTH, -1);
        }

        public static void putFetchMonth(int calendarMonth) {
            pref().put(KEY_SCHEDULE_FETCH_MONTH, calendarMonth);
        }
    }

    public static class Buildings {
        public static long downloadTime() {
            return pref().get(KEY_BUILDINGS_FETCH_TIME, 0l);
        }

        public static void putDownloadTime(long time) {
            pref().put(KEY_BUILDINGS_FETCH_TIME, time);
        }
    }

    public static class Data {
        public static String getPicturePathDefault() {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        }

        /**
         * 그림파일이 저장되는 경로를 얻는다.
         * <p>
         * <b>반환되는 경로 끝에는 '/' 이 붙지 않으므로 사용할 때 주의하여야 한다.</b>
         */
        @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        public static String getPicturePath() {
            return pref().get(KEY_IMAGE_SAVE_PATH, getPicturePathDefault());
        }

        public static void putPicturePath(String path) {
            pref().put(KEY_IMAGE_SAVE_PATH, path);
        }

        public static String getDocumentPathDefault() {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        }

        /**
         * 문서파일이 저장되는 경로를 얻는다.
         * <p>
         * <b>반환되는 경로 끝에는 '/' 이 붙지 않으므로 사용할 때 주의하여야 한다.</b>
         */
        @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        public static String getDocumentPath() {
            return pref().get(KEY_TXT_SAVE_PATH, getDocumentPathDefault());
        }

        public static void putDocumentPath(String path) {
            pref().put(KEY_TXT_SAVE_PATH, path);
        }

        /**
         * 파일이 저장되는 경로를 얻는다.
         * <p>
         * <b>반환되는 경로 끝에는 '/' 이 붙지 않으므로 사용할 때 주의하여야 한다.</b>
         *
         * @param key 파일에 해당하는 키
         */
        public static String getPathDefault(final String key) {
            switch (key) {
                default:
                case KEY_IMAGE_SAVE_PATH:
                    return getPicturePathDefault();
                case KEY_TXT_SAVE_PATH:
                    return getDocumentPathDefault();
            }
        }

        //@RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        @SuppressWarnings("ResourceType")
        public static String getPath(final String key) {
            switch (key) {
                default:
                case KEY_IMAGE_SAVE_PATH:
                    return getPicturePath();
                case KEY_TXT_SAVE_PATH:
                    return getDocumentPath();
            }
        }

        public static void putPath(final String key, String path) {
            switch (key) {
                default:
                case KEY_IMAGE_SAVE_PATH:
                    putPicturePath(path);
                    break;
                case KEY_TXT_SAVE_PATH:
                    putDocumentPath(path);
                    break;
            }
        }
    }


}
