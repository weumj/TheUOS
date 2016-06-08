package com.uoscs09.theuos2.http;

import android.text.TextUtils;
import android.util.SparseArray;

import com.uoscs09.theuos2.tab.announce.AnnounceItem;
import com.uoscs09.theuos2.tab.booksearch.BookItem;
import com.uoscs09.theuos2.tab.booksearch.BookStateInfo;
import com.uoscs09.theuos2.tab.booksearch.BookStates;
import com.uoscs09.theuos2.tab.buildings.BuildingRoom;
import com.uoscs09.theuos2.tab.buildings.ClassroomTimeTable;
import com.uoscs09.theuos2.tab.emptyroom.EmptyRoom;
import com.uoscs09.theuos2.tab.emptyroom.EmptyRoomInfo;
import com.uoscs09.theuos2.tab.libraryseat.SeatInfo;
import com.uoscs09.theuos2.tab.restaurant.RestItem;
import com.uoscs09.theuos2.tab.restaurant.WeekRestItem;
import com.uoscs09.theuos2.tab.schedule.UnivScheduleInfo;
import com.uoscs09.theuos2.tab.schedule.UnivScheduleItem;
import com.uoscs09.theuos2.tab.subject.CoursePlanInfo;
import com.uoscs09.theuos2.tab.subject.CoursePlanItem;
import com.uoscs09.theuos2.tab.subject.SubjectInformation;
import com.uoscs09.theuos2.tab.subject.SubjectItem2;
import com.uoscs09.theuos2.tab.subject.TimeTableSubjectInfo;
import com.uoscs09.theuos2.tab.timetable.ParseTimetable;
import com.uoscs09.theuos2.tab.timetable.SubjectInfoItem;
import com.uoscs09.theuos2.tab.timetable.Timetable2;
import com.uoscs09.theuos2.util.IOUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.StringUtil;
import com.uoscs09.theuos2.util.TaskUtil;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mj.android.utils.task.Task;
import mj.android.utils.task.Tasks;

import static com.uoscs09.theuos2.api.ApiService.URL_REST_WEEK;
import static com.uoscs09.theuos2.api.ApiService.URL_SCHOLARSHIP;
import static com.uoscs09.theuos2.api.ApiService.URL_SEATS;
import static com.uoscs09.theuos2.api.ApiService.announceApi;
import static com.uoscs09.theuos2.api.ApiService.libraryApi;
import static com.uoscs09.theuos2.api.ApiService.oApi;
import static com.uoscs09.theuos2.api.ApiService.restaurantApi;

// 네트워크와 파싱 관련된 작업만 수행하고, 파일 IO같은 작업은 AppResources에서 처리.
public class NetworkRequests {

    public static class Announces {
        // private static final ParseAnnounce PARSER = ParseAnnounce.getParser();
        // private static final ParseAnnounce SCHOLARSHIP_PARSER = ParseAnnounce.getScholarshipParser();

        public enum Category {

            GENERAL("FA1"),  // 일반공지
            AFFAIRS("FA2"), // 학사공지
            SCHOLARSHIP("SCHOLARSHIP"), // 장학공지
            EMPLOY("FA34") // 채용공지
            ;

            public final String tag;

            Category(String tag) {
                this.tag = tag;
            }

        }

        public static Task<List<AnnounceItem>> normalRequest(int category, int page) {
            return normalRequest(Category.values()[category - 1], page);
        }

        public static Task<List<AnnounceItem>> normalRequest(Category category, int pageIndex) {
            boolean scholarship = category == Category.SCHOLARSHIP;
            if (scholarship) {
                return announceApi().scholarships(URL_SCHOLARSHIP, pageIndex, 1, null, null);
            } else {
                return announceApi().announces(category.tag, pageIndex, null, null);
            }
        }

        public static Task<List<AnnounceItem>> searchRequest(int category, int pageIndex, String query) {
            return searchRequest(Category.values()[category - 1], pageIndex, query);
        }

        public static Task<List<AnnounceItem>> searchRequest(Category category, int pageIndex, String query) {
            boolean scholarship = category == Category.SCHOLARSHIP;
            if (scholarship) {
                return announceApi().scholarships(URL_SCHOLARSHIP, pageIndex, 1, "title", query);
            } else {
                return announceApi().announces(category.tag, pageIndex, "1", query);
            }
        }

        public static Task<File> attachedFileDownloadRequest(String url, String docPath) {
            return HttpRequest.Builder.newConnectionRequestBuilder(url)
                    .setHttpMethod(HttpRequest.HTTP_METHOD_POST)
                    .build()
                    .wrap(new HttpRequest.FileDownloadProcessor(new File(docPath)));
        }
    }

    public static class Books {
        public static Task<List<BookStateInfo>> requestBookStateInfo(String url) {
            return libraryApi().bookStateInformation(url).wrap(BookStates::bookStateList);
        }

        public static Task<List<BookItem>> request(String query, int page, int os, int oi) {
            String OS = getSpinnerItemString(1, os);
            String OI = getSpinnerItemString(0, oi);

            if (TextUtils.isEmpty(OI) && TextUtils.isEmpty(OS)) {
                return libraryApi().books(page, query);
            } else {
                return libraryApi().books(page, query, OI, OS);
            }
        }

        private static String getSpinnerItemString(int which, int pos) {
            switch (which) {
                case 0:
                    switch (pos) {
                        case 1:
                            return "DISP01";
                        case 2:
                            return "DISP02";
                        case 3:
                            return "DISP03";
                        case 4:
                            return "DISP04";
                        case 5:
                            return "DISP06";
                    }
                case 1:
                    switch (pos) {
                        case 1:
                            return "ASC";
                        case 2:
                            return "DESC";
                    }
                default:
                    return StringUtil.NULL;
            }
        }
    }

    public static class Restaurants {
        public static Task<SparseArray<RestItem>> request() {
            return restaurantApi().restItem();
        }

        public static Task<WeekRestItem> requestWeekInfo(String code) {
            return restaurantApi().weekRestItem(URL_REST_WEEK, code);
        }

    }

    public static class TimeTables {

        public static Task<Timetable2> request(CharSequence id, CharSequence passwd, OApiUtil.Semester semester, CharSequence year) {
            return TimeTableHttpRequest.newRequest(id, passwd, semester, year)
                    .wrap(httpURLConnection -> {
                        InputStream inputStream = httpURLConnection.getInputStream();
                        try {
                            return new ParseTimetable().parse(inputStream);
                        } finally {
                            IOUtil.closeStream(inputStream);
                            httpURLConnection.disconnect();
                        }
                    });
        }

    }

    public static class LibrarySeats {
        public static Task<SeatInfo> request() {
            return libraryApi().seatInformation(URL_SEATS);
        }
    }

    public static class EmptyRooms {

        public static Task<List<EmptyRoom>> request(String building, int time, int term) {
            if (building.equals("00")) {
                return requestAllEmptyRoom(time, term);
            } else {
                Calendar c = Calendar.getInstance();
                String date = new SimpleDateFormat("yyyyMMdd", Locale.KOREAN).format(new Date());
                String wdayTime = String.valueOf(c.get(Calendar.DAY_OF_WEEK)) + (time < 10 ? "0" : StringUtil.NULL) + String.valueOf(time);

                return oApi().emptyRooms(
                        OApiUtil.UOS_API_KEY,
                        OApiUtil.getYear(),
                        OApiUtil.Semester.getCodeByTermIndex(term),
                        building,
                        date,
                        date,
                        wdayTime,
                        null,
                        "Y"
                ).wrap(EmptyRoomInfo::emptyRoomList);
            }
        }


        private static Task<List<EmptyRoom>> requestAllEmptyRoom(int time, int term) {
            return Tasks.newTask(() -> {

                final String[] buildings = {
                        "01", "02", "03", "04", "05",
                        "06", "08", "09", "10", "11",
                        "13", "14", "15", "16", "17",
                        "18", "19", "20", "23", "24",
                        "25", "33"
                };

                String year = OApiUtil.getYear();
                String termCode = OApiUtil.Semester.getCodeByTermIndex(term);
                Calendar c = Calendar.getInstance();
                String date = new SimpleDateFormat("yyyyMMdd", Locale.KOREAN).format(new Date());
                String wdayTime = String.valueOf(c.get(Calendar.DAY_OF_WEEK)) + (time < 10 ? "0" : StringUtil.NULL) + String.valueOf(time);

                ArrayList<Task<List<EmptyRoom>>> requests = new ArrayList<>(buildings.length);

                for (String building : buildings) {
                    requests.add(oApi().emptyRooms(
                            OApiUtil.UOS_API_KEY,
                            year,
                            termCode,
                            building,
                            date,
                            date,
                            wdayTime,
                            null,
                            "Y"
                    ).wrap(EmptyRoomInfo::emptyRoomList));
                }

                return TaskUtil.parallelTaskTypedCollection(requests).get();
            });
        }
    }

    public static class Subjects {
        public static Task<List<SubjectItem2>> requestCulture(String year, int term, String subjectDiv, String subjectName) {
            return oApi().timetableCulture(
                    OApiUtil.UOS_API_KEY,
                    year,
                    OApiUtil.Semester.getCodeByTermIndex(term),
                    subjectDiv,
                    null,
                    subjectName
            ).wrap(TimeTableSubjectInfo::subjectInfoList);
        }

        public static Task<List<SubjectItem2>> requestMajor(String year, int term, Map<String, String> majorParams, String subjectName) {
            return oApi().timetableMajor(
                    OApiUtil.UOS_API_KEY,
                    year,
                    OApiUtil.Semester.getCodeByTermIndex(term),
                    majorParams.get("deptDiv"),
                    majorParams.get("dept"),
                    majorParams.get("subDept"),
                    majorParams.get("subjectDiv"),
                    majorParams.get("subjectNo"),
                    majorParams.get("classDiv"),
                    subjectName,
                    null
            ).wrap(TimeTableSubjectInfo::subjectInfoList);
        }


        public static Task<List<CoursePlanItem>> requestCoursePlan(SubjectItem2 item) {
            return oApi().coursePlans(
                    OApiUtil.UOS_API_KEY,
                    item.term,
                    item.subject_no,
                    item.class_div,
                    item.year
            ).wrap(CoursePlanInfo::coursePlanList);
        }


        public static Task<List<SubjectInfoItem>> requestSubjectInfo(String subjectName, int year, String termCode) {
            return oApi().subjectInformation(
                    OApiUtil.UOS_API_KEY,
                    year,
                    termCode,
                    subjectName,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            ).wrap(SubjectInformation::subjectInfoList);
        }

    }

    public static class UnivSchedules {
        public static Task<List<UnivScheduleItem>> request() {
            return oApi().schedules(OApiUtil.UOS_API_KEY).wrap(UnivScheduleInfo::univScheduleList);
        }
    }

    public static class Buildings {
        public static Task<BuildingRoom> buildingRooms() {
            return oApi().buildings(OApiUtil.UOS_API_KEY);
        }

        public static Task<ClassroomTimeTable> classRoomTimeTables(String year, String term, BuildingRoom.RoomInfo roomInfo) {
            return oApi().classRoomTimeTables(OApiUtil.UOS_API_KEY, year, term, roomInfo.buildingCode(), roomInfo.code());
        }
    }
}
