package com.uoscs09.theuos2.http;

import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.SparseArray;

import com.uoscs09.theuos2.oapi.UosOApi;
import com.uoscs09.theuos2.oapi.UosOApiService;
import com.uoscs09.theuos2.parse.XmlParser;
import com.uoscs09.theuos2.parse.XmlParserWrapper;
import com.uoscs09.theuos2.tab.announce.AnnounceItem;
import com.uoscs09.theuos2.tab.announce.ParseAnnounce;
import com.uoscs09.theuos2.tab.booksearch.BookItem;
import com.uoscs09.theuos2.tab.booksearch.BookStateInfo;
import com.uoscs09.theuos2.tab.booksearch.ParseBook;
import com.uoscs09.theuos2.tab.emptyroom.EmptyRoom;
import com.uoscs09.theuos2.tab.libraryseat.ParseSeat;
import com.uoscs09.theuos2.tab.libraryseat.SeatInfo;
import com.uoscs09.theuos2.tab.restaurant.ParseRest;
import com.uoscs09.theuos2.tab.restaurant.ParseRestaurantWeek;
import com.uoscs09.theuos2.tab.restaurant.RestItem;
import com.uoscs09.theuos2.tab.restaurant.WeekRestItem;
import com.uoscs09.theuos2.tab.schedule.UnivScheduleItem;
import com.uoscs09.theuos2.tab.subject.CoursePlanItem;
import com.uoscs09.theuos2.tab.subject.SubjectItem2;
import com.uoscs09.theuos2.tab.timetable.ParseTimeTable2;
import com.uoscs09.theuos2.tab.timetable.SubjectInfoItem;
import com.uoscs09.theuos2.tab.timetable.TimeTable;
import com.uoscs09.theuos2.tab.timetable.TimetableUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.OptimizeStrategy;
import com.uoscs09.theuos2.util.StringUtil;

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

// 오직 네트워크와 파싱 관련된 작업만 수행하고, 파일 IO같은 작업은 AppResources에서 처리.
public class NetworkRequests {

    private static UosOApi oapi() {
        return UosOApiService.api();
    }

    public static class Announces {
        private static final ParseAnnounce PARSER = ParseAnnounce.getParser();
        private static final ParseAnnounce SCHOLARSHIP_PARSER = ParseAnnounce.getScholarshipParser();

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
            ArrayMap<String, String> queryMap = new ArrayMap<>();

            String url;
            boolean scholarship = category == Category.SCHOLARSHIP;
            if (scholarship) {
                url = "http://scholarship.uos.ac.kr/scholarship/notice/notice/list.do";
                queryMap.put("brdBbsseq", "1");
            } else {
                url = "http://www.uos.ac.kr/korNotice/list.do";
                queryMap.put("list_id", category.tag);
            }
            queryMap.put("pageIndex", Integer.toString(pageIndex));

            return HttpRequest.Builder.newStringRequestBuilder(url)
                    .setHttpMethod(HttpRequest.HTTP_METHOD_POST)
                    .setParams(queryMap)
                    .build()
                    .wrap(scholarship ? SCHOLARSHIP_PARSER : PARSER);
        }

        public static Task<List<AnnounceItem>> searchRequest(int category, int pageIndex, String query) {
            return searchRequest(Category.values()[category - 1], pageIndex, query);
        }

        public static Task<List<AnnounceItem>> searchRequest(Category category, int pageIndex, String query) {
            ArrayMap<String, String> queryMap = new ArrayMap<>();

            String url;
            boolean scholarship = category == Category.SCHOLARSHIP;
            if (scholarship) {
                url = "http://scholarship.uos.ac.kr/scholarship/notice/notice/list.do";
                queryMap.put("brdBbsseq", "1");

                queryMap.put("sword", query);
                queryMap.put("skind", "title");
            } else {

                url = "http://www.uos.ac.kr/korNotice/list.do";
                queryMap.put("list_id", category.tag);

                queryMap.put("searchCnd", "1");
                queryMap.put("searchWrd", query);
            }
            queryMap.put("pageIndex", Integer.toString(pageIndex));

            return HttpRequest.Builder.newStringRequestBuilder(url)
                    .setHttpMethod(HttpRequest.HTTP_METHOD_POST)
                    .setParams(queryMap)
                    .build()
                    .wrap(scholarship ? SCHOLARSHIP_PARSER : PARSER);
        }

        public static Task<File> attachedFileDownloadRequest(String url, String docPath) {
            return HttpRequest.Builder.newConnectionRequestBuilder(url)
                    .setHttpMethod(HttpRequest.HTTP_METHOD_POST)
                    .build()
                    .wrap(new HttpRequest.FileDownloadProcessor(new File(docPath)));
        }
    }

    public static class Books {
        private static final ParseBook BOOK_PARSER = new ParseBook();
        private static final String URL = "http://mlibrary.uos.ac.kr/search/tot/result?sm=&st=KWRD&websysdiv=tot&si=TOTAL&pn=";

        private static final XmlParserWrapper<List<BookStateInfo>> BOOK_STATE_INFO_PARSER = new XmlParserWrapper<>(XmlParser.newReflectionParser(BookStateInfo.class, null, "location", "noholding", "item"));

        public static Task<List<BookStateInfo>> requestBookStateInfo(String url) {
            return HttpRequest.Builder.newConnectionRequestBuilder(url)
                    .build()
                    .wrap(BOOK_STATE_INFO_PARSER);
        }

        public static Task<List<BookItem>> request(String query, int page, int os, int oi) {
            return HttpRequest.Builder.newStringRequestBuilder(buildUrl(query, page, os, oi))
                    .build()
                    .wrap(BOOK_PARSER);
        }

        private static String buildUrl(String query, int page, int os, int oi) {
            String OS = getSpinnerItemString(1, os);
            String OI = getSpinnerItemString(0, oi);

            StringBuilder sb = new StringBuilder();
            sb.append(URL).append(page).append("&q=").append(query);
            String finalURL = null;

            String RM = "&websysdiv=tot";
            boolean check = true;
            if (!TextUtils.isEmpty(OI)) {
                sb.append("&oi=").append(OI);
                finalURL = StringUtil.remove(sb.toString(), RM);
                check = false;
            }
            if (!TextUtils.isEmpty(OS)) {
                sb.append("&os=").append(OS);
                finalURL = sb.toString();
                if (check) {
                    finalURL = StringUtil.remove(finalURL, RM);
                    check = false;
                }
            }

            if (check) {
                finalURL = sb.toString();
            }

            return finalURL;
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
        private static final ParseRest REST_PARSER = new ParseRest();
        private static final ParseRestaurantWeek RESTAURANT_WEEK_PARSER = new ParseRestaurantWeek();

        public static Task<SparseArray<RestItem>> request() {
            return HttpRequest.Builder
                    .newStringRequestBuilder("http://m.uos.ac.kr/mkor/food/list.do")
                    .build()
                    .wrap(REST_PARSER);
        }

        public static Task<WeekRestItem> requestWeekInfo(String code) {
            return HttpRequest.Builder.newStringRequestBuilder("http://www.uos.ac.kr/food/placeList.do?rstcde=" + code)
                    .build()
                    .wrap(RESTAURANT_WEEK_PARSER);
        }

    }

    public static class TimeTables {
        private static final XmlParserWrapper<TimeTable> PARSER = new XmlParserWrapper<>(new ParseTimeTable2());

        public static Task<TimeTable> request(CharSequence id, CharSequence passwd, OApiUtil.Semester semester, CharSequence year) {
            return TimeTableHttpRequest.newRequest(id, passwd, semester, year)
                    .wrap(PARSER)
                    .wrap(timeTable -> {
                        TimetableUtil.makeColorTable(timeTable);
                        timeTable.getClassTimeInformationTable();

                        return timeTable;
                    });
        }

    }

    public static class LibrarySeats {
        private static final ParseSeat LIBRARY_SEAR_PARSER = new ParseSeat();
        private final static String URL = "http://203.249.102.34:8080/seat/domian5.asp";

        public static Task<SeatInfo> request() {
            return HttpRequest.Builder.newStringRequestBuilder(URL)
                    .setResultEncoding(StringUtil.ENCODE_EUC_KR)
                    .build()
                    .wrap(LIBRARY_SEAR_PARSER);
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

                return oapi().emptyRooms(
                        OApiUtil.UOS_API_KEY,
                        OApiUtil.getYear(),
                        OApiUtil.Semester.getCodeByTermIndex(term),
                        building,
                        date,
                        date,
                        wdayTime,
                        null,
                        null
                );
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
                    requests.add(oapi().emptyRooms(
                            OApiUtil.UOS_API_KEY,
                            year,
                            termCode,
                            building,
                            date,
                            date,
                            wdayTime,
                            null,
                            null
                    ));
                }

                final int N = requests.size();
                if (OptimizeStrategy.isSafeToOptimize()) {
                    final int half = N / 2;
                    ArrayList<Task<List<EmptyRoom>>> tasks = new ArrayList<>();
                    tasks.add(subTask(requests.subList(0, half)));
                    tasks.add(subTask(requests.subList(half, N)));

                    return Tasks.Parallel.parallelTaskTypedCollection(tasks).get();
                } else {
                    return Tasks.Parallel.serialTaskTypedCollection(requests).get();
                }
            });
        }

        private static Task<List<EmptyRoom>> subTask(List<Task<List<EmptyRoom>>> tasks) {
            return Tasks.newTask(() -> {
                List<EmptyRoom> results = new ArrayList<>();

                for (Task<List<EmptyRoom>> request : tasks)
                    results.addAll(request.get());
                return results;
            });
        }
    }

    public static class Subjects {
        public static Task<List<SubjectItem2>> requestCulture(String year, int term, String subjectDiv, String subjectName) {
            return oapi().timetableCulture(
                    OApiUtil.UOS_API_KEY,
                    year,
                    OApiUtil.Semester.getCodeByTermIndex(term),
                    subjectDiv,
                    null,
                    subjectName
            );
        }

        public static Task<List<SubjectItem2>> requestMajor(String year, int term, Map<String, String> majorParams, String subjectName) {

            return oapi().timetableMajor(
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
            );
        }


        public static Task<List<CoursePlanItem>> requestCoursePlan(SubjectItem2 item) {

            return oapi().coursePlans(
                    OApiUtil.UOS_API_KEY,
                    item.term,
                    item.subject_no,
                    item.class_div,
                    item.year
            );
        }


        public static Task<List<SubjectInfoItem>> requestSubjectInfo(String subjectName, int year, String termCode) {
            return UosOApiService.api().subjectInformation(
                    OApiUtil.UOS_API_KEY,
                    Integer.toString(year),
                    termCode,
                    subjectName,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

    }

    public static class UnivSchedules {
        public static Task<List<UnivScheduleItem>> request() {
            return oapi().schedules(OApiUtil.UOS_API_KEY);
        }
    }
}
