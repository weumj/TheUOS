package com.uoscs09.theuos2.util;

import android.content.Context;
import android.util.SparseArray;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.async.AbstractRequest;
import com.uoscs09.theuos2.async.AsyncUtil;
import com.uoscs09.theuos2.async.Request;
import com.uoscs09.theuos2.common.SerializableArrayMap;
import com.uoscs09.theuos2.http.NetworkRequests;
import com.uoscs09.theuos2.tab.announce.AnnounceItem;
import com.uoscs09.theuos2.tab.booksearch.BookItem;
import com.uoscs09.theuos2.tab.booksearch.BookStateInfo;
import com.uoscs09.theuos2.tab.emptyroom.EmptyClassRoomItem;
import com.uoscs09.theuos2.tab.libraryseat.SeatInfo;
import com.uoscs09.theuos2.tab.libraryseat.SeatItem;
import com.uoscs09.theuos2.tab.restaurant.RestItem;
import com.uoscs09.theuos2.tab.restaurant.WeekRestItem;
import com.uoscs09.theuos2.tab.schedule.UnivScheduleItem;
import com.uoscs09.theuos2.tab.subject.CoursePlanItem;
import com.uoscs09.theuos2.tab.subject.SubjectItem2;
import com.uoscs09.theuos2.tab.timetable.SubjectInfoItem;
import com.uoscs09.theuos2.tab.timetable.TimeTable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;

public class AppResources {

    public static class Announces {
        public static Request<ArrayList<AnnounceItem>> normalRequest(Context context, int category, int page) {
            return NetworkRequests.Announces.normalRequest(context, category, page)
                    .wrap(announceItems -> {
                        if (PrefUtil.getInstance(context).get(PrefUtil.KEY_ANNOUNCE_EXCEPT_TYPE_NOTICE, false)) {
                            final int size = announceItems.size();
                            for (int i = size - 1; i >= 0; i--) {
                                AnnounceItem item = announceItems.get(i);
                                if (item.isTypeNotice())
                                    announceItems.remove(i);
                            }
                        }

                        return announceItems;
                    });
        }

        public static Request<ArrayList<AnnounceItem>> searchRequest(Context context, int category, int pageIndex, String query) {
            return NetworkRequests.Announces.searchRequest(context, category, pageIndex, query)
                    .wrap(announceItems -> {
                        final int size = announceItems.size();
                        for (int i = size - 1; i >= 0; i--) {
                            AnnounceItem item = announceItems.get(i);
                            if (item.isTypeNotice())
                                announceItems.remove(i);
                        }

                        return announceItems;
                    });
        }
    }


    public static class Books {
        public static Request<ArrayList<BookStateInfo>> requestBookStateInfo(Context context, String url) {
            return NetworkRequests.Books.requestBookStateInfo(context, url);
        }

        public static Request<ArrayList<BookItem>> request(Context context, String query, int page, int os, int oi) {
            return NetworkRequests.Books.request(context, query, page, os, oi)
                    .wrap(originalList -> {
                                if (PrefUtil.getInstance(context).get(PrefUtil.KEY_CHECK_BORROW, false) && originalList.size() > 0) {
                                    ArrayList<BookItem> newList = new ArrayList<>();
                                    String emptyMsg = context.getString(R.string.tab_book_not_found);
                                    final int N = originalList.size();
                                    for (int i = 0; i < N; i++) {
                                        BookItem item = originalList.get(i);
                                        if (item.isBookAvailable()) {
                                            newList.add(item);
                                        }
                                    }

                                    if (newList.size() == 0) {
                                        BookItem item = new BookItem();
                                        item.bookInfo = emptyMsg;
                                        newList.add(item);
                                    }

                                    return newList;
                                } else {
                                    return new ArrayList<>(originalList);
                                }
                            }
                    );
        }
    }


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

                    return NetworkRequests.Restaurants.request(context)
                            .wrap(restItemSparseArray -> {
                                SerializableArrayMap<Integer, RestItem> writingObject = SerializableArrayMap.fromSparseArray(restItemSparseArray);
                                IOUtil.writeObjectToFile(context, IOUtil.FILE_REST, writingObject);
                                PrefUtil.getInstance(context).put(PrefUtil.KEY_REST_DATE_TIME, OApiUtil.getDate());
                                return restItemSparseArray;
                            })
                            .get();
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

                return NetworkRequests.Restaurants.requestWeekInfo(context, code)
                        .wrap(IOUtil.<WeekRestItem>newInternalFileWriteProcessor(context, WEEK_FILE_NAME + code))
                        .wrap(item -> {
                            AppResources.Restaurants.putValueIntoPref(prefUtil, code, item);
                            return item;
                        })
                        .get();
            });

        }

    }


    public static class TimeTables {

        public static Request<TimeTable> request(Context context, CharSequence id, CharSequence passwd, OApiUtil.Semester semester, CharSequence year) {
            return NetworkRequests.TimeTables.request(context, id, passwd, semester, year)
                    .wrap(IOUtil.<TimeTable>newInternalFileWriteProcessor(context, IOUtil.FILE_TIMETABLE));
        }

        public static Request<TimeTable> readFromFile(Context context) {
            return AsyncUtil.newRequest(() -> IOUtil.readFromFileSuppressed(context, IOUtil.FILE_TIMETABLE));
        }

    }


    public static class LibrarySeats {
        public static Request<SeatInfo> request(Context context) {
            return NetworkRequests.LibrarySeats.request(context)
                    .wrap(seatInfo -> {

                        if (PrefUtil.getInstance(context).get(PrefUtil.KEY_CHECK_SEAT, false)) {
                            ArrayList<SeatItem> list = seatInfo.seatItemList;
                            // 스터디룸 인덱스
                            final int[] filterArr = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 23, 24, 25, 26, 27, 28};
                            final int size = filterArr.length;
                            for (int i = size - 1; i > -1; i--) {
                                SeatItem item = list.get(filterArr[i]);
                                if (item.utilizationRate >= 50)
                                    list.remove(item);
                            }
                        }

                        return seatInfo;
                    });
        }
    }


    public static class EmptyRooms {
        public static Request<ArrayList<EmptyClassRoomItem>> request(Context context, String building, int time, int term) {
            return NetworkRequests.EmptyRooms.request(context, building, time, term);
        }
    }


    public static class Subjects {
        public static Request<ArrayList<SubjectItem2>> requestCulture(Context context, String year, int term, String subjectDiv, String subjectName) {
            return NetworkRequests.Subjects.requestCulture(context, year, term, subjectDiv, subjectName);
        }

        public static Request<ArrayList<SubjectItem2>> requestMajor(Context context, String year, int term, Map<String, String> majorParams, String subjectName) {
            return NetworkRequests.Subjects.requestMajor(context, year, term, majorParams, subjectName);
        }

        public static Request<ArrayList<CoursePlanItem>> requestCoursePlan(Context context, SubjectItem2 item) {
            return NetworkRequests.Subjects.requestCoursePlan(context, item);
        }

        public static Request<ArrayList<SubjectInfoItem>> requestSubjectInfo(Context context, String subjectName, int year, String termCode) {
            return NetworkRequests.Subjects.requestSubjectInfo(context, subjectName, year, termCode);
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

                    return NetworkRequests.UnivSchedules.request(context)
                            .wrap(IOUtil.<ArrayList<UnivScheduleItem>>newInternalFileWriteProcessor(context, AppResources.UnivSchedules.FILE_NAME))
                            .wrap(univScheduleItems -> {
                                        pref.put(PrefUtil.KEY_SCHEDULE_FETCH_MONTH, univScheduleItems.get(0).getDate(true).get(Calendar.MONTH));
                                        return univScheduleItems;
                                    }
                            )
                            .get();
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