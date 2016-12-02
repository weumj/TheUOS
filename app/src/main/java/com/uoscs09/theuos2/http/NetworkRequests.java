package com.uoscs09.theuos2.http;

import android.text.TextUtils;
import android.util.SparseArray;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.api.WiseApiService;
import com.uoscs09.theuos2.tab.announce.AnnounceItem;
import com.uoscs09.theuos2.tab.booksearch.BookItem;
import com.uoscs09.theuos2.tab.booksearch.BookStateInfo;
import com.uoscs09.theuos2.tab.booksearch.BookStateWrapper;
import com.uoscs09.theuos2.tab.buildings.BuildingRoom;
import com.uoscs09.theuos2.tab.buildings.ClassRoomTimetable;
import com.uoscs09.theuos2.tab.emptyroom.EmptyRoom;
import com.uoscs09.theuos2.tab.emptyroom.EmptyRoomWrapper;
import com.uoscs09.theuos2.tab.libraryseat.SeatTotalInfo;
import com.uoscs09.theuos2.tab.restaurant.RestItem;
import com.uoscs09.theuos2.tab.restaurant.RestWeekItem;
import com.uoscs09.theuos2.tab.schedule.UnivScheduleItem;
import com.uoscs09.theuos2.tab.schedule.UnivScheduleWrapper;
import com.uoscs09.theuos2.tab.subject.CoursePlan;
import com.uoscs09.theuos2.tab.subject.CoursePlanWrapper;
import com.uoscs09.theuos2.tab.subject.Subject;
import com.uoscs09.theuos2.tab.subject.SubjectWrapper;
import com.uoscs09.theuos2.tab.subject.TimeTableSubjectInfo;
import com.uoscs09.theuos2.tab.timetable.SimpleSubject;
import com.uoscs09.theuos2.tab.timetable.Timetable2;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.StringUtil;
import com.uoscs09.theuos2.util.TaskUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mj.android.utils.task.Task;
import mj.android.utils.task.Tasks;

import static com.uoscs09.theuos2.api.UosApiService.URL_M_ANNOUNCE;
import static com.uoscs09.theuos2.api.UosApiService.URL_M_SCHOLARSHIP;
import static com.uoscs09.theuos2.api.UosApiService.URL_REST_WEEK;
import static com.uoscs09.theuos2.api.UosApiService.URL_SEATS;
import static com.uoscs09.theuos2.api.UosApiService.announceApi;
import static com.uoscs09.theuos2.api.UosApiService.libraryApi;
import static com.uoscs09.theuos2.api.UosApiService.oApi;
import static com.uoscs09.theuos2.api.UosApiService.restaurantApi;

// 네트워크와 파싱 관련된 작업만 수행하고, 파일 IO 같은 작업은 되도록 AppResources 에서 처리.
public class NetworkRequests {

    public static class Announces {
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
             /*
            if (scholarship) {
                return PrefHelper.Announces.isSearchOnMobile() ?
                        announceApi().scholarshipsMobile(URL_M_SCHOLARSHIP, pageIndex, 1) :
                        announceApi().scholarships(URL_SCHOLARSHIP, pageIndex, 1, null, null);
            } else {
                return PrefHelper.Announces.isSearchOnMobile() ?
                        announceApi().announcesMobile(URL_M_ANNOUNCE, category.tag, pageIndex, null, null) :
                        announceApi().announces(category.tag, pageIndex, null, null);
            }
            */

            if (scholarship) {
                return announceApi().scholarshipsMobile(URL_M_SCHOLARSHIP, pageIndex, 1);

            } else {
                return announceApi().announcesMobile(URL_M_ANNOUNCE, category.tag, pageIndex, null, null);
            }
        }

        public static Task<List<AnnounceItem>> searchRequest(int category, int pageIndex, String query) {
            return searchRequest(Category.values()[category - 1], pageIndex, query);
        }

        public static Task<List<AnnounceItem>> searchRequest(Category category, int pageIndex, String query) {
            boolean scholarship = category == Category.SCHOLARSHIP;
            /*
            if (scholarship) {
                return PrefHelper.Announces.isSearchOnMobile() ?
                        Tasks.newTask(() -> {
                            throw new IllegalStateException(AppUtil.context().getString(R.string.tab_announce_not_support_search_on_scholarship));
                        }) :
                        announceApi().scholarships(URL_M_SCHOLARSHIP, pageIndex, 1, "1", query);
            } else {
                return PrefHelper.Announces.isSearchOnMobile() ?
                        announceApi().announcesMobile(URL_M_ANNOUNCE, category.tag, pageIndex, "1", query) :
                        announceApi().announces(category.tag, pageIndex, "1", query);
            }
            */


            if (scholarship) {
                return Tasks.newTask(() -> {
                    throw new IllegalStateException(AppUtil.context().getString(R.string.tab_announce_not_support_search_on_scholarship));
                });
            } else {
                return announceApi().announcesMobile(URL_M_ANNOUNCE, category.tag, pageIndex, "1", query);

            }
        }

        public static Task<File> attachedFileDownloadRequest(String url, String docPath, String fileName) {
            return new HttpTask.Builder(url)
                    .setHttpMethod(HttpTask.HTTP_METHOD_POST)
                    .buildAsHttpURLConnection()
                    .map(new HttpTask.FileDownloadProcessor(new File(docPath), fileName));
        }
    }

    public static class Books {
        public static Task<List<BookStateInfo>> requestBookStateInfo(String url) {
            return libraryApi().bookStateInformation(url).map(BookStateWrapper::bookStateList);
        }

        public static Task<List<BookItem>> request(String query, int page, int os, int oi) {
            String OS = optionItem(1, os);
            String OI = optionItem(0, oi);

            if (TextUtils.isEmpty(OI) && TextUtils.isEmpty(OS)) {
                return libraryApi().books(page, query);
            } else {
                return libraryApi().books(page, query, OI, OS);
            }
        }

        private static String optionItem(int which, int pos) {
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
                    return "";
            }
        }
    }

    public static class Restaurants {
        public static Task<SparseArray<RestItem>> request() {
            return restaurantApi().restItem();
        }

        public static Task<RestWeekItem> requestWeekInfo(String code) {
            return restaurantApi().weekRestItem(URL_REST_WEEK, code);
        }

    }

    public static class TimeTables {

        public static Task<Timetable2> request(CharSequence id, CharSequence passwd, OApiUtil.Semester semester, CharSequence year) {
            return WiseApiService.timetableTask(id, passwd, semester, Integer.parseInt(year.toString()));
        }

    }

    public static class LibrarySeats {
        public static Task<SeatTotalInfo> request() {
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
                String wdayTime = String.valueOf(c.get(Calendar.DAY_OF_WEEK)) + (time < 10 ? "0" : "") + String.valueOf(time);

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
                ).map(EmptyRoomWrapper::emptyRoomList);
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
                String wdayTime = String.valueOf(c.get(Calendar.DAY_OF_WEEK)) + (time < 10 ? "0" : "") + String.valueOf(time);

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
                    ).map(EmptyRoomWrapper::emptyRoomList));
                }

                return TaskUtil.parallelTaskTypedCollection(requests).get();
            });
        }
    }

    public static class Subjects {
        public static Task<List<Subject>> requestCulture(String year, int term, String subjectDiv, String subjectName) {
            return oApi().timetableCulture(
                    OApiUtil.UOS_API_KEY,
                    year,
                    OApiUtil.Semester.getCodeByTermIndex(term),
                    subjectDiv,
                    null,
                    StringUtil.encodeEucKr(subjectName) // 한글은 euc-kr로 인코딩 하여야 함.
            ).map(TimeTableSubjectInfo::subjectInfoList);
        }

        public static Task<List<Subject>> requestMajor(String year, int term, Map<String, String> majorParams, String subjectName) {
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
                    StringUtil.encodeEucKr(subjectName), // 한글은 euc-kr로 인코딩 하여야 함.
                    null
            ).map(TimeTableSubjectInfo::subjectInfoList);
        }


        public static Task<List<CoursePlan>> requestCoursePlan(Subject item) {
            return oApi().coursePlans(
                    OApiUtil.UOS_API_KEY,
                    item.term,
                    item.subject_no,
                    item.class_div,
                    item.year
            ).map(CoursePlanWrapper::coursePlanList);
        }


        public static Task<List<SimpleSubject>> requestSubjectInfo(String subjectName, int year, String termCode) {
            return oApi().subjectInformation(
                    OApiUtil.UOS_API_KEY,
                    year,
                    termCode,
                    StringUtil.encodeEucKr(subjectName), // 한글은 euc-kr로 인코딩 하여야 함.
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            ).map(SubjectWrapper::subjectInfoList);
        }

    }

    public static class UnivSchedules {
        public static Task<List<UnivScheduleItem>> request() {
            return oApi().schedules(OApiUtil.UOS_API_KEY).map(UnivScheduleWrapper::univScheduleList);
        }
    }

    public static class Buildings {
        public static Task<BuildingRoom> buildingRooms() {
            return oApi().buildings(OApiUtil.UOS_API_KEY);
        }

        public static Task<ClassRoomTimetable> classRoomTimeTables(String year, String term, BuildingRoom.RoomInfo roomInfo) {
            return oApi().classRoomTimeTables(OApiUtil.UOS_API_KEY, year, term, roomInfo.buildingCode(), roomInfo.code());
        }
    }

    public static class WiseScores {
        public static Task<com.uoscs09.theuos2.tab.score.WiseScores> wiseScores(String id, String pw) {
            return Tasks.Parallel.serialTask(
                    WiseApiService.mobileWiseApi().login(1, id, pw),
                    WiseApiService.mobileWiseApi().score()
            ).map(objects -> (com.uoscs09.theuos2.tab.score.WiseScores) objects[1]);
        }
    }
}
