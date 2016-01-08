package com.uoscs09.theuos2.util;

import android.content.Context;
import android.util.SparseArray;

import com.uoscs09.theuos2.async.AbstractRequest;
import com.uoscs09.theuos2.async.AsyncUtil;
import com.uoscs09.theuos2.async.Request;
import com.uoscs09.theuos2.common.SerializableArrayMap;
import com.uoscs09.theuos2.http.NetworkRequests;
import com.uoscs09.theuos2.tab.restaurant.RestItem;
import com.uoscs09.theuos2.tab.restaurant.WeekRestItem;
import com.uoscs09.theuos2.tab.schedule.UnivScheduleItem;
import com.uoscs09.theuos2.tab.timetable.TimeTable;
import com.uoscs09.theuos2.tab.timetable.TimetableUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class AppResources {
    public static class Restaurants {

        public static Request<SparseArray<RestItem>> request(Context context, boolean shouldForceUpdate) {
            return new AbstractRequest<SparseArray<RestItem>>() {
                @Override
                public SparseArray<RestItem> get() throws Exception {
                    if (!shouldForceUpdate && OApiUtil.getDateTime() - PrefUtil.getInstance(context).get(PrefUtil.KEY_REST_DATE_TIME, 0) < 3) {
                        try {
                            SparseArray<RestItem> result = readFromFile(context);

                            if (result.size() > 0)
                                return result;

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    return NetworkRequests.Restaurants.request(context).get();
                }
            };
        }

        public static SparseArray<RestItem> readFromFile(Context context) {
            return SerializableArrayMap.toSparseArray(IOUtil.readFromFileSuppressed(context, IOUtil.FILE_REST));
        }

        private static int[] getValueFromPref(PrefUtil prefUtil, String code) {
            int today = OApiUtil.getDate();
            return new int[]{prefUtil.get(PrefUtil.KEY_REST_WEEK_FETCH_TIME + "_START_" + code, today + 1),
                    prefUtil.get(PrefUtil.KEY_REST_WEEK_FETCH_TIME + "_END_" + code, today - 1)};
        }

        public static void putValueIntoPref(PrefUtil prefUtil, String code, WeekRestItem item) {
            prefUtil.put(PrefUtil.KEY_REST_WEEK_FETCH_TIME + "_START_" + code, item.startDate);
            prefUtil.put(PrefUtil.KEY_REST_WEEK_FETCH_TIME + "_END_" + code, item.endDate);
        }

        public static final String WEEK_FILE_NAME = "FILE_REST_WEEK_ITEM";

        public static Request<WeekRestItem> readWeekInfo(Context context, String code, boolean shouldUpdateUsingInternet) {
            PrefUtil prefUtil = PrefUtil.getInstance(context);

            int today = OApiUtil.getDate();
            final int[] recodedDate = getValueFromPref(prefUtil, code);

            return AsyncUtil.newRequest(() -> {
                // 이번주의 식단이 기록된 파일이 있으면, 인터넷에서 가져오지 않고 그 파일을 읽음
                if (!shouldUpdateUsingInternet && ((recodedDate[0] <= today) && (today <= recodedDate[1]))) {

                    WeekRestItem result = new IOUtil.Builder<WeekRestItem>(WEEK_FILE_NAME + code)
                            .setContext(context)
                            .build()
                            .get();

                    if (result != null)
                        return result;
                }

                return NetworkRequests.Restaurants.requestWeekInfo(context, code).get();
            });

        }

    }


    public static class TimeTables {
        public static Request<TimeTable> readFromFile(Context context) {
            return AsyncUtil.newRequest(() -> TimetableUtil.readTimetable(context));
        }

    }

    public static class UnivSchedules {
        public static final String FILE_NAME = "file_univ_schedule";

        public static Request<ArrayList<UnivScheduleItem>> request(Context context) {
            return new AbstractRequest<ArrayList<UnivScheduleItem>>() {
                @Override
                public ArrayList<UnivScheduleItem> get() throws Exception {
                    PrefUtil pref = PrefUtil.getInstance(context);

                    // 이번 달의 일정이 기록된 파일이 있으면, 인터넷에서 가져오지 않고 그 파일을 읽음
                    if (pref.get(PrefUtil.KEY_SCHEDULE_FETCH_MONTH, -1) == Calendar.getInstance().get(Calendar.MONTH)) {
                        ArrayList<UnivScheduleItem> result = new IOUtil.Builder<ArrayList<UnivScheduleItem>>(FILE_NAME)
                                .setContext(context)
                                .build()
                                .get();

                        if (result != null)
                            return result;

                    }

                    return NetworkRequests.UnivSchedules.request(context).get();
                }
            }.wrap(univScheduleItems -> {
                Collections.sort(univScheduleItems, (lhs, rhs) -> {
                    int lDay = lhs.dateStart.day, rDay = rhs.dateStart.day;
                    if (lDay == rDay)
                        return 0;
                    else if (lDay > rDay)
                        return 1;
                    else
                        return -1;
                });

                return univScheduleItems;
            });
        }
    }
}