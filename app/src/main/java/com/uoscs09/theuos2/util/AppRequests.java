package com.uoscs09.theuos2.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.RequiresPermission;
import android.util.SparseArray;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.appwidget.timetable.TimeTableWidget;
import com.uoscs09.theuos2.common.SerializableArrayMap;
import com.uoscs09.theuos2.http.NetworkRequests;
import com.uoscs09.theuos2.tab.announce.AnnounceDetailItem;
import com.uoscs09.theuos2.tab.announce.AnnounceItem;
import com.uoscs09.theuos2.tab.booksearch.BookDetailItem;
import com.uoscs09.theuos2.tab.booksearch.BookItem;
import com.uoscs09.theuos2.tab.booksearch.BookStateInfo;
import com.uoscs09.theuos2.tab.buildings.BuildingRoom;
import com.uoscs09.theuos2.tab.buildings.ClassRoomTimetable;
import com.uoscs09.theuos2.tab.emptyroom.EmptyRoom;
import com.uoscs09.theuos2.tab.libraryseat.SeatInfo;
import com.uoscs09.theuos2.tab.libraryseat.SeatTotalInfo;
import com.uoscs09.theuos2.tab.restaurant.RestItem;
import com.uoscs09.theuos2.tab.restaurant.RestWeekItem;
import com.uoscs09.theuos2.tab.schedule.UnivScheduleItem;
import com.uoscs09.theuos2.tab.subject.CoursePlan;
import com.uoscs09.theuos2.tab.subject.Subject;
import com.uoscs09.theuos2.tab.timetable.SimpleSubject;
import com.uoscs09.theuos2.tab.timetable.Timetable2;
import com.uoscs09.theuos2.tab.timetable.TimetableUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import io.reactivex.exceptions.Exceptions;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.uoscs09.theuos2.util.AppUtil.context;

public class AppRequests {

    public static class Announces {
        public static Observable<AnnounceDetailItem> announceInfo(int category, String url) {
            return NetworkRequests.Announces.announceInfo(category, url)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        public static Observable<List<AnnounceItem>> normalRequest(int category, int page) {
            return NetworkRequests.Announces.normalRequest(category, page)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            /*
                    .map(announceItems -> {
                        if (PrefHelper.Announces.isAnnounceExceptNoticeType()) {
                            final int size = announceItems.size();
                            for (int i = size - 1; i >= 0; i--) {
                                AnnounceItem item = announceItems.get(i);
                                if (item.isTypeNotice())
                                    announceItems.remove(i);
                            }
                        }

                        return announceItems;
                    });
                    */
        }

        public static Observable<List<AnnounceItem>> searchRequest(int category, int pageIndex, String query) {
            return NetworkRequests.Announces.searchRequest(category, pageIndex, query)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            /*
                    .map(announceItems -> {
                        final int size = announceItems.size();
                        for (int i = size - 1; i >= 0; i--) {
                            AnnounceItem item = announceItems.get(i);
                            if (item.isTypeNotice())
                                announceItems.remove(i);
                        }

                        return announceItems;
                    });
                    */
        }


        public static Observable<File> attachedFileDownload(String url, String docPath, String fileName) {
            return NetworkRequests.Announces.attachedFileDownloadRequest(url, docPath, fileName)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }


    public static class Books {
        public static Observable<BookDetailItem> bookDetailItem(BookItem bookItem) {
            return NetworkRequests.Books.bookDetailItem(bookItem)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        public static Observable<List<BookStateInfo>> requestBookStateInfo(String url) {
            return NetworkRequests.Books.requestBookStateInfo(url)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        public static Observable<List<BookItem>> request(String query, int page, int os, int oi) {
            return NetworkRequests.Books.request(query, page, os, oi)
                    .map(originalList -> {
                                //FIXME
                                if (PrefHelper.Books.isFilterUnavailableBook() && originalList.size() > 0) {
                                    List<BookItem> newList = new ArrayList<>();
                                    String emptyMsg = context().getString(R.string.tab_book_not_found);
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
                    ).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }


    public static class Restaurants {

        public static Observable<SparseArray<RestItem>> request(boolean shouldForceUpdate) {
            // TODO fix
            if (!shouldForceUpdate && PrefHelper.Restaurants.isDownloadTimeWithin(3)) {
                return file().flatMap(result -> {
                    if (result.size() > 0) {
                        return Observable.just(result);
                    } else {
                        return network();
                    }
                });
            }

            return network();
        }


        private static Observable<SparseArray<RestItem>> network() {
            return NetworkRequests.Restaurants.request()
                    .map(restItemSparseArray -> {
                        SerializableArrayMap<Integer, RestItem> writingObject = SerializableArrayMap.fromSparseArray(restItemSparseArray);
                        try {
                            IOUtil.writeObjectToInternalFile(IOUtil.FILE_REST, writingObject);
                        } catch (IOException e) {
                            throw Exceptions.propagate(e);
                        }
                        PrefHelper.Restaurants.putDownloadTime(OApiUtil.getDateTime());
                        return restItemSparseArray;
                    }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        private static Observable<SparseArray<RestItem>> file() {
            return Observable.fromCallable(Restaurants::readFromFile)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        public static SparseArray<RestItem> readFromFile() {
            return SerializableArrayMap.toIntegerKeySparseArray(IOUtil.readInternalFileSilent(IOUtil.FILE_REST));
        }

        public static Observable<RestWeekItem> readWeekInfo(String code) {
            return NetworkRequests.Restaurants.requestWeekInfo(code)
                    .map(item -> {
                        PrefHelper.Restaurants.putWeekItemFetchTime(code, item);
                        return item;
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());

        }

    }


    public static class TimeTables {

        public static Observable<Timetable2> dummyRequest(CharSequence id, CharSequence passwd, OApiUtil.Semester semester, CharSequence year) {
            return Observable.error(
                    new Exception(String.format("Dummy Request - [\nid : %s\nsemester : %s\nyear : %s\n]", id, semester.name(), year))
            );
        }


        public static Observable<Timetable2> request(CharSequence id, CharSequence passwd, OApiUtil.Semester semester, CharSequence year) {
            return NetworkRequests.TimeTables.request(id, passwd, semester, year)
                    .map(data -> {
                        try {
                            IOUtil.<Timetable2>newInternalFileWriteFunc(IOUtil.FILE_TIMETABLE).func(data);
                        } catch (Throwable throwable) {
                            throw Exceptions.propagate(throwable);
                        }
                        TimeTableWidget.sendRefreshIntent(context());
                        return data;
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        public static Observable<Timetable2> readFile() {
            return Observable.fromCallable(() -> (Timetable2) IOUtil.readInternalFileSilent(IOUtil.FILE_TIMETABLE))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        public static Observable<Boolean> deleteTimetable() {
            return Observable.fromCallable(() -> {
                Context context = context();
                boolean result = context.deleteFile(IOUtil.FILE_TIMETABLE);
                TimetableUtil.clearTimeTableColor(context);
                //TimetableAlarmUtil.clearAllAlarm(context);
                TimeTableWidget.sendRefreshIntent(context);
                return result;
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }

        /* 반환값은 저장된 파일의 경로*/
        @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        public static Observable<String> saveTimetableToImage(Timetable2 timetable, ListView listView, ListAdapter originalAdapter, View header) {
            //noinspection ResourceType
            final String picturePath = PrefHelper.Data.getPicturePath();
            @SuppressLint("DefaultLocale")
            String savedPath = String.format("%s/timetable_%d_%s_%d.png", picturePath, timetable.year(), timetable.semester().name(), System.currentTimeMillis());

            return new ImageUtil.ListViewBitmapRequest.Builder(listView, originalAdapter)
                    .setHeaderView(header)
                    .build()
                    .flatMap(bitmap -> Observable.fromCallable(() -> new ImageUtil.ImageWriteProcessor(savedPath).func(bitmap)))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }


    public static class LibrarySeats {
        public static Observable<SeatTotalInfo> request() {
            return NetworkRequests.LibrarySeats.request()
                    .map(seatInfo -> {
                        //FIXME
                        if (PrefHelper.LibrarySeats.isFilterOccupyingRoom()) {
                            List<SeatInfo> list = seatInfo.seatInfoList();
                            // 스터디룸 인덱스
                            final int[] filterArr = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 23, 24, 25, 26, 27, 28};
                            final int size = filterArr.length;
                            for (int i = size - 1; i > -1; i--) {
                                SeatInfo item = list.get(filterArr[i]);
                                if (item.utilizationRate >= 50)
                                    list.remove(item);
                            }
                        }

                        return seatInfo;
                    }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        private final static int[] STUDY_ROOM_NUMBER_ARRAY = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 23, 24, 25, 26, 27, 28};

        public static Observable<List<SeatInfo>> widgetDataRequest() {
            return NetworkRequests.LibrarySeats.request()
                    .map(SeatTotalInfo::seatInfoList)
                    .map(list -> {
                        //FIXME
                        // filter
                        if (PrefHelper.LibrarySeats.isShowingStudyRoomInWidget()) {
                            List<SeatInfo> newList = new ArrayList<>();
                            for (int i : STUDY_ROOM_NUMBER_ARRAY) {
                                SeatInfo item = list.get(i);
                                // if (Double.parseDouble(item.utilizationRateStr) < 50d)
                                newList.add(item);
                            }
                            return newList;
                        } else {
                            return list;
                        }
                    }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        public static Observable<List<SeatInfo>> readFile() {
            //noinspection unchecked
            return Observable.fromCallable(() -> (List<SeatInfo>) IOUtil.readFromInternalFile(IOUtil.FILE_LIBRARY_SEAT))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }


    public static class EmptyRooms {
        public static Observable<List<EmptyRoom>> request(String building, int time, int term) {
            return NetworkRequests.EmptyRooms.request(building, time, term)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }


    public static class Subjects {
        public static Observable<List<Subject>> requestCulture(String year, int term, String subjectDiv, String subjectName) {
            return NetworkRequests.Subjects.requestCulture(year, term, subjectDiv, subjectName)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        public static Observable<List<Subject>> requestMajor(String year, int term, Map<String, String> majorParams, String subjectName) {
            return NetworkRequests.Subjects.requestMajor(year, term, majorParams, subjectName)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        public static Observable<List<CoursePlan>> requestCoursePlan(Subject item) {
            return NetworkRequests.Subjects.requestCoursePlan(item)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        public static Observable<List<SimpleSubject>> requestSubjectInfo(String subjectName, int year, String termCode) {
            return NetworkRequests.Subjects.requestSubjectInfo(subjectName, year, termCode)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        /* 반환값은 저장된 파일의 경로*/
        @SuppressWarnings("MissingPermission")
        @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        public static Observable<String> saveCoursePlanToImage(ListView listView, ListAdapter adapter, View header, final Subject subject) {
            final String picturePath = PrefHelper.Data.getPicturePath();
            final String fileName = String.format("%s/%s_%s_%s_%s.png", picturePath, context().getString(R.string.tab_course_plan_title), subject.subject_nm, subject.prof_nm, subject.class_div);
            return new ImageUtil.ListViewBitmapRequest.Builder(listView, adapter)
                    .setHeaderView(header)
                    .build()
                    .flatMap(bitmap -> Observable.fromCallable(() -> new ImageUtil.ImageWriteProcessor(fileName).func(bitmap)))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        /* 반환값은 저장된 파일의 경로*/
        @SuppressWarnings("MissingPermission")
        @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        public static Observable<String> saveCoursePlanToTextFile(final List<CoursePlan> infoList, final Subject subject) {

            final String docPath = PrefHelper.Data.getDocumentPath();
            final String fileName = String.format("%s/%s_%s_%s_%s.txt", docPath, context().getString(R.string.tab_course_plan_title), subject.subject_nm, subject.prof_nm, subject.class_div);

            return Observable.fromCallable(() -> {
                StringBuilder sb = new StringBuilder();
                writeHeader(sb, infoList.get(0), subject.getClassRoomTimeInformation());

                int size = infoList.size();
                for (int i = 0; i < size; i++) {
                    writeWeek(sb, infoList.get(i));
                }
                return sb.toString();
            }).map(s -> {
                try {
                    return IOUtil.newStringExternalFileWriteFunc(fileName).func(s);
                } catch (Throwable t) {
                    throw Exceptions.propagate(t);
                }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }


        private static void writeHeader(StringBuilder sb, CoursePlan course, String classRoomInformation) {
            Context context = context();

            sb.append(course.subject_nm);
            sb.append("\n");

            sb.append(course.subject_no);
            sb.append("\n");
            sb.append("\n");

            sb.append(context.getString(R.string.tab_course_plan_prof));
            sb.append(" : ");
            sb.append(StringUtil.emptyStringIfNull(course.prof_nm));
            sb.append("\n");

            sb.append(context.getString(R.string.tab_course_plan_location));
            sb.append(" : ");
            sb.append(StringUtil.emptyStringIfNull(classRoomInformation));
            sb.append("\n");

            sb.append(context.getString(R.string.tab_course_plan_prof_tel));
            sb.append(" : ");
            sb.append(StringUtil.emptyStringIfNull(course.tel_no));
            sb.append("\n");
            sb.append("\n");

            sb.append(context.getString(R.string.tab_course_plan_eval));
            sb.append(" : ");
            sb.append("\n");
            sb.append(StringUtil.emptyStringIfNull(course.score_eval_rate));
            sb.append("\n");
            sb.append("\n");

            sb.append(context.getString(R.string.tab_course_plan_book));
            sb.append(" : ");
            sb.append("\n");
            sb.append(StringUtil.emptyStringIfNull(course.book_nm));
            sb.append("\n");
            sb.append("\n");
        }

        private static void writeWeek(StringBuilder sb, CoursePlan item) {
            Context context = context();

            sb.append("---  ");
            sb.append(item.week);
            sb.append(context.getString(R.string.tab_course_week));
            sb.append("  ----------------------");
            sb.append("\n");
            sb.append("\n");

            sb.append("[");
            sb.append(context.getString(R.string.tab_course_week_class_cont));
            sb.append("]\n");
            sb.append(StringUtil.emptyStringIfNull(item.class_cont));
            sb.append("\n");
            sb.append("\n");

            sb.append("[");
            sb.append(context.getString(R.string.tab_course_week_class_meth));
            sb.append("]\n");
            sb.append(StringUtil.emptyStringIfNull(item.class_meth));
            sb.append("\n");
            sb.append("\n");

            sb.append("[");
            sb.append(context.getString(R.string.tab_course_week_book));
            sb.append("]\n");
            sb.append(StringUtil.emptyStringIfNull(item.week_book));
            sb.append("\n");
            sb.append("\n");

            sb.append("[");
            sb.append(context.getString(R.string.tab_course_week_prjt_etc));
            sb.append("]\n");
            sb.append(StringUtil.emptyStringIfNull(item.prjt_etc));
            sb.append("\n");
            sb.append("\n");
        }
    }


    public static class UnivSchedules {
        static final String FILE_NAME = "file_univ_schedule_r1";

        public static Observable<List<UnivScheduleItem>> request(boolean force) {
            //FIXME
            return Observable.fromCallable(() -> PrefHelper.UnivSchedules.isMonthEqualToFetchMonth() && !force)
                    .flatMap(shouldNetwork -> {
                        if (shouldNetwork) {
                            return network();
                        }

                        try {
                            //noinspection unchecked
                            ArrayList<UnivScheduleItem> result = (ArrayList<UnivScheduleItem>) IOUtil.internalFileOpenTask(FILE_NAME).get();

                            if (result != null)
                                return Observable.just(result);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }

                        return network();
                    }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        private static Observable<List<UnivScheduleItem>> network() {
            return NetworkRequests.UnivSchedules.request()
                    .map(univScheduleItems -> {
                        try {
                            IOUtil.<List<UnivScheduleItem>>newInternalFileWriteFunc(UnivSchedules.FILE_NAME).func(univScheduleItems);
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                        PrefHelper.UnivSchedules.putFetchMonth(univScheduleItems.get(0).getDate(true).get(Calendar.MONTH));
                        return univScheduleItems;
                    })
                    .flatMap(Observable::from)
                    .sorted((lhs, rhs) -> {
                        int lDay = lhs.dateStart.day, rDay = rhs.dateStart.day;
                        if (lDay == rDay)
                            return 0;
                        else if (lDay > rDay)
                            return 1;
                        else
                            return -1;
                    })
                    .toList()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
/*
        private static final String SELECTION = String.format("((%s = ?) AND (%s = ?) AND (%s = ?))",
                CalendarContract.Calendars.ACCOUNT_NAME, CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.Calendars.OWNER_ACCOUNT);
        private static final String[] EVENT_PROJECTION = {
                CalendarContract.Calendars._ID
        };

        @SuppressWarnings("ResourceType")
        @RequiresPermission(allOf = {Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR, Manifest.permission.GET_ACCOUNTS})
        public static Observable<Uri> addUnivScheduleToCalender(final Context context, final String account, final UnivScheduleItem mSelectedItem, final int itemIndex) {
            return Observable.fromCallable(() -> {
                if (TextUtils.isEmpty(account) || mSelectedItem == null) {
                    throw new NullPointerException("account or scheduleItem == null");
                }

                ContentResolver cr = context.getContentResolver();

                String[] selectionArgs = new String[]{account, "com.google", account};
                Cursor c = cr.query(CalendarContract.Calendars.CONTENT_URI, EVENT_PROJECTION, SELECTION, selectionArgs, null);

                if (c == null) {
                    throw new Exception(context.getString(R.string.tab_univ_schedule_calendar_not_exist));
                } else if (!c.moveToFirst()) {
                    c.close();
                    throw new Exception(context.getString(R.string.tab_univ_schedule_calendar_not_exist));
                }

                long calendarId = c.getLong(0);
                ContentValues cv = mSelectedItem.toContentValues(calendarId);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                    cv.put(CalendarContract.Events.EVENT_COLOR, ResourceUtil.getOrderedColor(context, itemIndex));

                Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, cv);
                c.close();

                return uri;
            });
        }
        */


    }

    public static class Buildings {
        private static final String FILE_BUILDINGS = "FILE_BUILDINGS";

        public static Observable<BuildingRoom> buildingRooms(boolean forceDownload) {
            // // FIXME
            return Observable.fromCallable(() -> {
                if (forceDownload) return true;

                long downloadedTimeL = PrefHelper.Buildings.downloadTime();

                if (downloadedTimeL > 0) {
                    Calendar current = Calendar.getInstance(), downloadedTime = Calendar.getInstance();
                    downloadedTime.setTimeInMillis(downloadedTimeL);

                    // 다운로드 한 날짜가 현재 시각보다 5개월 이전 이면
                    return Math.abs(current.get(Calendar.MONTH) - downloadedTime.get(Calendar.MONTH)) >= 5;
                }

                return true;
            }).flatMap(shouldDownload -> {
                if (!shouldDownload) {
                    try {
                        BuildingRoom buildingRoom = IOUtil.readFromInternalFile(FILE_BUILDINGS);
                        if (buildingRoom != null) {
                            buildingRoom.afterParsing();
                            return Observable.just(buildingRoom);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return downloadBuildingRooms();
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }

        private static Observable<BuildingRoom> downloadBuildingRooms() {
            return NetworkRequests.Buildings.buildingRooms()
                    .map(room -> {
                        PrefHelper.Buildings.putDownloadTime(System.currentTimeMillis());
                        return room;
                    })
                    .map(buildingRoom -> {
                        try {
                            return (BuildingRoom) IOUtil.newInternalFileWriteFunc(FILE_BUILDINGS).func(buildingRoom);
                        } catch (Throwable t) {
                            throw Exceptions.propagate(t);
                        }
                    }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        public static Observable<ClassRoomTimetable> classRoomTimeTables(String year, String term, EmptyRoom emptyRoom, boolean forceDownload) {
            return buildingRooms(forceDownload).flatMap(room -> {
                int findingRoomNum = Integer.valueOf(emptyRoom.roomNo.split("-")[0]);
                //todo search
                /* roomName 에 대해 정렬 되어있지 않으므로 사용 불가
                int findingRoomNum = Integer.valueOf(emptyRoom.roomNo.split("-")[0]);


                BuildingRoom.RoomInfo info = new BuildingRoom.RoomInfo("", emptyRoom.roomNo, "" + (findingRoomNum < 10 ? "0" + findingRoomNum : findingRoomNum));

                int index = Collections.binarySearch(room.getRoomInfoList(), info, (lhs, rhs) -> {
                    int buildingCodeCompared = lhs.buildingCode().compareTo(rhs.buildingCode());

                    if (buildingCodeCompared == 0) {
                        return lhs.roomName().compareTo(rhs.roomName());
                    } else
                        return buildingCodeCompared;
                });

                if (index != -1)
                    return classRoomTimeTables(year, term, room.getRoomInfoList().get(index)).get();
                */

                final String[] buildings = {
                        "01", "02", "03", "04", "05",
                        "06", "08", "09", "10", "11",
                        "13", "14", "15", "16", "17",
                        "18", "19", "20", "23", "24",
                        "25", "33"
                };

                return Observable.from(buildings)
                        .map(room::roomInfoList)
                        .filter(pair -> pair != null && Integer.valueOf(pair.buildingInfo().code()) == findingRoomNum)
                        .flatMap(pair -> Observable.from(pair.roomInfoList()))
                        .filter(roomInfo -> roomInfo.roomName().contains(emptyRoom.roomNo))
                        .flatMap(roomInfo -> classRoomTimeTables(year, term, roomInfo))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            });
        }

        public static Observable<ClassRoomTimetable> classRoomTimeTables(String year, String term, BuildingRoom.RoomInfo roomInfo) {
            return NetworkRequests.Buildings.classRoomTimeTables(year, term, roomInfo)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

    public static class WiseScores {
        public static Observable<com.uoscs09.theuos2.tab.score.WiseScores> wiseScores(String id, String pw) {
            return NetworkRequests.WiseScores.wiseScores(id, pw)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }
}