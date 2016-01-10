package com.uoscs09.theuos2.http;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;

import com.uoscs09.theuos2.async.AsyncUtil;
import com.uoscs09.theuos2.async.Request;
import com.uoscs09.theuos2.parse.XmlParser;
import com.uoscs09.theuos2.parse.XmlParserWrapper;
import com.uoscs09.theuos2.tab.announce.AnnounceItem;
import com.uoscs09.theuos2.tab.announce.ParseAnnounce;
import com.uoscs09.theuos2.tab.booksearch.BookItem;
import com.uoscs09.theuos2.tab.booksearch.BookStateInfo;
import com.uoscs09.theuos2.tab.booksearch.ParseBook;
import com.uoscs09.theuos2.tab.emptyroom.EmptyClassRoomItem;
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.FutureTask;

// 오직 네트워크와 파싱 관련된 작업만 수행하고, 파일 IO같은 작업은 AppResources에서 처리.
public class NetworkRequests {
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

        public static Request<ArrayList<AnnounceItem>> normalRequest(Context context, int category, int page) {
            return normalRequest(context, Category.values()[category - 1], page);
        }

        public static Request<ArrayList<AnnounceItem>> normalRequest(Context context, Category category, int pageIndex) {
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
                    .checkNetworkState(context)
                    .wrap(scholarship ? SCHOLARSHIP_PARSER : PARSER);
        }

        public static Request<ArrayList<AnnounceItem>> searchRequest(Context context, int category, int pageIndex, String query) {
            return searchRequest(context, Category.values()[category - 1], pageIndex, query);
        }

        public static Request<ArrayList<AnnounceItem>> searchRequest(Context context, Category category, int pageIndex, String query) {
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
                    .checkNetworkState(context)
                    .wrap(scholarship ? SCHOLARSHIP_PARSER : PARSER);
        }

        public static Request<File> attachedFileDownloadRequest(Context context, String url, String docPath) {
            return HttpRequest.Builder.newConnectionRequestBuilder(url)
                    .setHttpMethod(HttpRequest.HTTP_METHOD_POST)
                    .build()
                    .checkNetworkState(context)
                    .wrap(new HttpRequest.FileDownloadProcessor(new File(docPath)));
        }
    }

    public static class Books {
        private static final ParseBook BOOK_PARSER = new ParseBook();
        private static final String URL = "http://mlibrary.uos.ac.kr/search/tot/result?sm=&st=KWRD&websysdiv=tot&si=TOTAL&pn=";

        private static final XmlParserWrapper<ArrayList<BookStateInfo>> BOOK_STATE_INFO_PARSER = new XmlParserWrapper<>(XmlParser.newReflectionParser(BookStateInfo.class, "location", "noholding", "item"));

        public static Request<ArrayList<BookStateInfo>> requestBookStateInfo(Context context, String url) {
            return HttpRequest.Builder.newConnectionRequestBuilder(url)
                    .build()
                    .checkNetworkState(context)
                    .wrap(BOOK_STATE_INFO_PARSER);
        }

        public static Request<List<BookItem>> request(Context context, String query, int page, int os, int oi) {
            return HttpRequest.Builder.newStringRequestBuilder(buildUrl(query, page, os, oi))
                    .build()
                    .checkNetworkState(context)
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
            if (!OI.equals(StringUtil.NULL)) {
                sb.append("&oi=").append(OI);
                finalURL = StringUtil.remove(sb.toString(), RM);
                check = false;
            }
            if (!OS.equals(StringUtil.NULL)) {
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

        public static Request<SparseArray<RestItem>> request(Context context) {
            return HttpRequest.Builder
                    .newStringRequestBuilder("http://m.uos.ac.kr/mkor/food/list.do")
                    .build()
                    .checkNetworkState(context)
                    .wrap(REST_PARSER);
        }

        public static Request<WeekRestItem> requestWeekInfo(Context context, String code) {
            return HttpRequest.Builder.newStringRequestBuilder("http://www.uos.ac.kr/food/placeList.do?rstcde=" + code)
                    .build()
                    .checkNetworkState(context)
                    .wrap(RESTAURANT_WEEK_PARSER);
        }

    }

    public static class TimeTables {
        private static final XmlParserWrapper<TimeTable> PARSER = new XmlParserWrapper<>(new ParseTimeTable2());

        public static Request<TimeTable> request(Context context, CharSequence id, CharSequence passwd, OApiUtil.Semester semester, CharSequence year) {
            return TimeTableHttpRequest.newRequest(context, id, passwd, semester, year)
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

        public static Request<SeatInfo> request(Context context) {
            return HttpRequest.Builder.newStringRequestBuilder(URL)
                    .setResultEncoding(StringUtil.ENCODE_EUC_KR)
                    .build()
                    .checkNetworkState(context)
                    .wrap(LIBRARY_SEAR_PARSER);
        }
    }

    public static class EmptyRooms {
        private static final String URL = "http://wise.uos.ac.kr/uosdoc/api.ApiUcsFromToEmptyRoom.oapi";

        private static final XmlParserWrapper<ArrayList<EmptyClassRoomItem>> EMPTY_ROOM_PARSER = OApiUtil.getParser(EmptyClassRoomItem.class);

        private static Map<String, String> buildParamsMap(String building, int time, int term) {
            Calendar c = Calendar.getInstance();
            String wdayTime = String.valueOf(c.get(Calendar.DAY_OF_WEEK)) + (time < 10 ? "0" : StringUtil.NULL) + String.valueOf(time);

            ArrayMap<String, String> params = new ArrayMap<>(9);

            String date = new SimpleDateFormat("yyyyMMdd", Locale.KOREAN).format(new Date());
            params.put(OApiUtil.API_KEY, OApiUtil.UOS_API_KEY);
            params.put(OApiUtil.YEAR, OApiUtil.getYear());
            params.put("dateFrom", date);
            params.put("dateTo", date);
            params.put("classRoomDiv", StringUtil.NULL);
            params.put("aplyPosbYn", "Y");

            params.put("wdayTime", wdayTime);
            params.put(OApiUtil.TERM, OApiUtil.Semester.values()[term].code);
            params.put("building", building);

            return params;
        }

        public static Request<ArrayList<EmptyClassRoomItem>> request(Context context, String building, int time, int term) {
            if (building.equals("00")) {
                return requestAllEmptyRoom(context, time, term);
            } else {
                return HttpRequest.Builder.newConnectionRequestBuilder(URL)
                        .setParams(buildParamsMap(building, time, term))
                        .setParamsEncoding(StringUtil.ENCODE_EUC_KR)
                        .build()
                        .checkNetworkState(context)
                        .wrap(EMPTY_ROOM_PARSER);
            }
        }


        private static Request<ArrayList<EmptyClassRoomItem>> requestAllEmptyRoom(Context context, int time, int term) {
            return AsyncUtil.newRequest(() -> {
                ArrayList<EmptyClassRoomItem> list = new ArrayList<>();
                final String[] buildings = {
                        "01", "02", "03", "04", "05",
                        "06", "08", "09", "10", "11",
                        "13", "14", "15", "16", "17",
                        "18", "19", "20", "23", "24", "25", "33"};

                ArrayList<Request<ArrayList<EmptyClassRoomItem>>> requests = new ArrayList<>(buildings.length);

                Map<String, String> params = buildParamsMap("00", time, term);

                HttpRequest.Builder<HttpURLConnection> requestBuilder = HttpRequest.Builder.newConnectionRequestBuilder(URL)
                        .setParams(params)
                        .setParamsEncoding(StringUtil.ENCODE_EUC_KR);

                for (String bd : buildings) {
                    params.put("building", bd);
                    requests.add(requestBuilder.build().wrap(EMPTY_ROOM_PARSER));
                }

                HttpRequest.checkNetworkStateAndThrowException(context);

                final int N = requests.size();
                final int half = N / 2;
                if (OptimizeStrategy.isSafeToOptimize(context)) {
                    FutureTask<ArrayList<EmptyClassRoomItem>> task1, task2;

                    task1 = new FutureTask<>(() -> {
                        List<Request<ArrayList<EmptyClassRoomItem>>> firstHalfRequests = requests.subList(0, half);
                        ArrayList<EmptyClassRoomItem> results = new ArrayList<>();

                        for (Request<ArrayList<EmptyClassRoomItem>> request : firstHalfRequests)
                            results.addAll(request.get());
                        return results;
                    });

                    task2 = new FutureTask<>(() -> {
                        List<Request<ArrayList<EmptyClassRoomItem>>> secondHalfRequests = requests.subList(half, N);
                        ArrayList<EmptyClassRoomItem> results = new ArrayList<>();

                        for (Request<ArrayList<EmptyClassRoomItem>> request : secondHalfRequests)
                            results.addAll(request.get());
                        return results;
                    });

                    AsyncUtil.executeFor(task1);
                    //AsyncUtil.executeFor(task2);
                    task2.run();

                    for (; ; ) {
                        try {
                            list.addAll(task1.get());
                            break;
                        } catch (InterruptedException e) {
                            Log.e("EmptyRoomRequest", "interrupted TASK #1", e);
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }
                    }


                    for (; ; ) {
                        try {
                            list.addAll(task2.get());
                            break;
                        } catch (InterruptedException e) {
                            Log.e("EmptyRoomRequest", "interrupted TASK #2", e);
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }
                    }

                } else {
                    for (int i = 0; i < N; i++)
                        list.addAll(requests.get(i).get());
                }


                return list;
            });
        }
    }

    public static class Subjects {
        private static final XmlParserWrapper<ArrayList<SubjectItem2>> SUBJECT_PARSER = OApiUtil.getParser(SubjectItem2.class);
        private static final String SUBJECT_CULTURE = "http://wise.uos.ac.kr/uosdoc/api.ApiUcrCultTimeInq.oapi";
        private static final String SUBJECT_MAJOR = "http://wise.uos.ac.kr/uosdoc/api.ApiUcrMjTimeInq.oapi";

        public static Request<ArrayList<SubjectItem2>> requestCulture(Context context, String year, int term, String subjectDiv, String subjectName) {
            ArrayMap<String, String> map = new ArrayMap<>(5);

            map.put(OApiUtil.API_KEY, OApiUtil.UOS_API_KEY);
            map.put(OApiUtil.YEAR, year);
            map.put(OApiUtil.TERM, OApiUtil.Semester.values()[term].code);
            map.put("subjectDiv", subjectDiv);

            try {
                String encodedSubjectName = URLEncoder.encode(subjectName, StringUtil.ENCODE_EUC_KR);
                map.put("subjectNm", encodedSubjectName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return HttpRequest.Builder.newConnectionRequestBuilder(SUBJECT_CULTURE)
                    .setParamsEncoding(StringUtil.ENCODE_EUC_KR)
                    .setParams(map)
                    .build()
                    .checkNetworkState(context)
                    .wrap(SUBJECT_PARSER);
        }

        public static Request<ArrayList<SubjectItem2>> requestMajor(Context context, String year, int term, Map<String, String> majorParams, String subjectName) {
            ArrayMap<String, String> map = new ArrayMap<>();

            map.put(OApiUtil.API_KEY, OApiUtil.UOS_API_KEY);
            map.put(OApiUtil.YEAR, year);
            map.put(OApiUtil.TERM, OApiUtil.Semester.values()[term].code);
            if (majorParams != null)
                map.putAll(majorParams);

            try {
                String encodedSubjectName = URLEncoder.encode(subjectName, StringUtil.ENCODE_EUC_KR);
                map.put("subjectNm", encodedSubjectName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return HttpRequest.Builder.newConnectionRequestBuilder(SUBJECT_MAJOR)
                    .setParamsEncoding(StringUtil.ENCODE_EUC_KR)
                    .setParams(map)
                    .build()
                    .checkNetworkState(context)
                    .wrap(SUBJECT_PARSER);
        }


        private final static String COURSE_PLAN_URL = "http://wise.uos.ac.kr/uosdoc/api.ApiApiCoursePlanView.oapi";
        private static final XmlParserWrapper<ArrayList<CoursePlanItem>> COURSE_PLAN_PARSER = OApiUtil.getParser(CoursePlanItem.class);

        public static Request<ArrayList<CoursePlanItem>> requestCoursePlan(Context context, SubjectItem2 item) {
            ArrayMap<String, String> params = new ArrayMap<>(5);

            params.put(OApiUtil.API_KEY, OApiUtil.UOS_API_KEY);
            params.put(OApiUtil.TERM, item.term);
            params.put(OApiUtil.SUBJECT_NO, item.subject_no);
            params.put(OApiUtil.CLASS_DIV, item.class_div);
            params.put(OApiUtil.YEAR, item.year);

            return HttpRequest.Builder.newConnectionRequestBuilder(COURSE_PLAN_URL)
                    .setParams(params)
                    .setParamsEncoding(StringUtil.ENCODE_EUC_KR)
                    .build()
                    .checkNetworkState(context)
                    .wrap(COURSE_PLAN_PARSER);
        }

        private static final String SUBJECT_INFO_URL = "http://wise.uos.ac.kr/uosdoc/api.ApiApiSubjectList.oapi";
        private static final XmlParserWrapper<ArrayList<SubjectInfoItem>> SUBJECT_INFO_PARSER = OApiUtil.getParser(SubjectInfoItem.class);

        public static Request<ArrayList<SubjectInfoItem>> requestSubjectInfo(Context context, String subjectName, int year, String termCode) {
            ArrayMap<String, String> params = new ArrayMap<>();
            params.put(OApiUtil.API_KEY, OApiUtil.UOS_API_KEY);
            params.put(OApiUtil.SUBJECT_NAME, subjectName);
            params.put(OApiUtil.YEAR, Integer.toString(year));
            params.put(OApiUtil.TERM, termCode);

            return HttpRequest.Builder.newConnectionRequestBuilder(SUBJECT_INFO_URL)
                    .setParams(params)
                    .setParamsEncoding(StringUtil.ENCODE_EUC_KR)
                    .build()
                    .checkNetworkState(context)
                    .wrap(SUBJECT_INFO_PARSER);
        }

    }

    public static class UnivSchedules {
        private static final XmlParserWrapper<ArrayList<UnivScheduleItem>> UNIV_SCHEDULE_PARSER = OApiUtil.getUnivScheduleParser();

        private static final String URL = OApiUtil.URL_API_MAIN_DB + '?' + OApiUtil.API_KEY + '=' + OApiUtil.UOS_API_KEY;

        public static Request<ArrayList<UnivScheduleItem>> request(Context context) {
            return HttpRequest.Builder.newConnectionRequestBuilder(URL)
                    .build()
                    .checkNetworkState(context)
                    .wrap(UNIV_SCHEDULE_PARSER);
        }
    }
}
